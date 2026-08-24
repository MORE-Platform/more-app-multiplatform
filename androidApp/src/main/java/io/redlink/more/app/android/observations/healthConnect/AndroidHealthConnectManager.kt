package io.redlink.more.app.android.observations.healthConnect

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.core.net.toUri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc
import io.github.aakira.napier.Napier
import io.redlink.more.SharedRes
import io.redlink.more.dialog.AlertController
import io.redlink.more.dialog.AlertDialogModel
import io.redlink.more.observations.healthConnect.HealthConnectDataType
import io.redlink.more.observations.healthConnect.model.HealthConnectSample
import io.redlink.more.scopes.Scope
import io.redlink.more.services.store.PermissionApprovalState
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Instant
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

object AndroidHealthConnectManager {
    private const val PROVIDER_PACKAGE_NAME = "com.google.android.apps.healthdata"

    enum class Metric(val permission: String) {
        HEART_RATE(HealthPermission.getReadPermission(HeartRateRecord::class)),
        STEPS(HealthPermission.getReadPermission(StepsRecord::class)),
        DISTANCE(HealthPermission.getReadPermission(DistanceRecord::class))
    }

    /**
     * Every Health Connect metric a subtype needs, including bonus fields requested in the same
     * permission dialog (steps also reads distance for the daily aggregate). Single source of
     * truth so the permission-request path and each collector's own `requestPermission()` cannot
     * diverge.
     */
    fun metrics(dataType: HealthConnectDataType): Set<Metric> = when (dataType) {
        HealthConnectDataType.HEART_RATE -> setOf(Metric.HEART_RATE)
        HealthConnectDataType.STEPS -> setOf(Metric.STEPS, Metric.DISTANCE)
    }

    private val permissionRequestMutex = Mutex()

    private var launcherOwnerToken: Any? = null
    private var permissionLauncher: ActivityResultLauncher<Set<String>>? = null
    private var ownerLifecycle: Lifecycle? = null
    private var pendingContinuation: CancellableContinuation<Set<String>>? = null
    private var inFlightMetrics: Set<Metric>? = null
    private val pendingPermissionRequest = mutableSetOf<Metric>()
    private val deniedMetrics = mutableSetOf<Metric>()

    /**
     * Registers the permission launcher with the activity lifecycle. Requests made while the
     * owning activity isn't RESUMED (e.g. mid-transition between activities) are queued and
     * replayed the next time it resumes, instead of being launched from an activity that may be
     * torn down before the system permission UI returns a result.
     *
     * The activity itself is not retained by this singleton. Instead, an opaque
     * token is returned which the activity uses to clean up its registration.
     */
    fun initializePermissionLauncher(activity: ComponentActivity): Any {
        val ownerToken = Any()

        launcherOwnerToken = ownerToken
        ownerLifecycle = activity.lifecycle

        permissionLauncher = activity.registerForActivityResult(
            PermissionController.createRequestPermissionResultContract()
        ) { granted ->
            recordPermissionResult(granted)
            pendingContinuation?.resumeWith(Result.success(granted))
            pendingContinuation = null
            inFlightMetrics = null
        }

        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                replayPendingRequestIfAny(activity)
            }
        })

        return ownerToken
    }

    fun cleanupPermissionLauncher(ownerToken: Any) {
        if (launcherOwnerToken === ownerToken) {
            permissionLauncher = null
            launcherOwnerToken = null
            ownerLifecycle = null

            inFlightMetrics?.let { metrics ->
                synchronized(pendingPermissionRequest) {
                    pendingPermissionRequest += metrics
                }
            }
            inFlightMetrics = null

            pendingContinuation?.cancel()
            pendingContinuation = null
        }
    }

    private fun recordPermissionResult(granted: Set<String>) {
        val requestedMetrics = inFlightMetrics ?: return
        synchronized(deniedMetrics) {
            requestedMetrics.forEach { metric ->
                if (metric.permission in granted) {
                    deniedMetrics -= metric
                } else {
                    deniedMetrics += metric
                }
            }
        }
    }

    fun isHealthConnectAvailable(context: Context): Boolean =
        HealthConnectClient.getSdkStatus(
            context.applicationContext,
            PROVIDER_PACKAGE_NAME
        ) == HealthConnectClient.SDK_AVAILABLE

    suspend fun permissionState(
        context: Context,
        metric: Metric
    ): PermissionApprovalState {
        val client = client(context) ?: return PermissionApprovalState.NOT_SET
        val granted = client.permissionController.getGrantedPermissions()

        return when {
            metric.permission in granted -> PermissionApprovalState.GRANTED
            synchronized(deniedMetrics) { metric in deniedMetrics } -> PermissionApprovalState.DECLINED
            else -> PermissionApprovalState.NOT_SET
        }
    }

    suspend fun requestPermission(
        context: Context,
        metric: Metric
    ) = requestPermissions(context, setOf(metric))

    suspend fun requestPermissions(
        context: Context,
        metrics: Set<Metric>
    ) = permissionRequestMutex.withLock {
        if (metrics.isEmpty()) {
            return@withLock
        }

        if (!isHealthConnectAvailable(context)) {
            promptInstall(context.applicationContext)
            return@withLock
        }

        val launcher = permissionLauncher
        val ownerResumed = ownerLifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == true

        if (launcher == null || !ownerResumed) {
            Napier.w("HC: launcher not ready (launcher=${launcher != null}, ownerResumed=$ownerResumed), queueing")

            synchronized(pendingPermissionRequest) {
                pendingPermissionRequest += metrics
            }

            return@withLock
        }

        val client = HealthConnectClient.getOrCreate(
            context.applicationContext
        )

        val requestedPermissions =
            metrics.map { it.permission }.toSet()

        val alreadyGranted =
            client.permissionController.getGrantedPermissions()

        val missingPermissions =
            requestedPermissions - alreadyGranted

        if (missingPermissions.isEmpty()) {
            return@withLock
        }

        suspendCancellableCoroutine<Set<String>> { continuation ->
            pendingContinuation = continuation
            inFlightMetrics = metrics

            continuation.invokeOnCancellation {
                if (pendingContinuation === continuation) {
                    pendingContinuation = null
                }
            }

            Scope.launch {
                withContext(Dispatchers.Main.immediate) {
                    launcher.launch(missingPermissions)
                }
            }
        }
    }

    private fun replayPendingRequestIfAny(activity: ComponentActivity) {
        val metrics = synchronized(pendingPermissionRequest) {
            if (pendingPermissionRequest.isEmpty()) {
                return
            }

            val set = pendingPermissionRequest.toSet()
            pendingPermissionRequest.clear()
            set
        }

        activity.lifecycleScope.launch {
            requestPermissions(
                activity.applicationContext,
                metrics
            )
        }
    }

    suspend fun readSamples(
        context: Context,
        metric: Metric,
        from: Instant,
        to: Instant
    ): List<HealthConnectSample> {
        val client = client(context) ?: return emptyList()

        val range = TimeRangeFilter.between(
            from.toJavaInstant(),
            to.toJavaInstant()
        )

        return when (metric) {
            Metric.HEART_RATE -> client.readRecords(
                ReadRecordsRequest(
                    HeartRateRecord::class,
                    range
                )
            ).records.flatMap { record ->
                record.samples.map { sample ->
                    HealthConnectSample.HeartRate(
                        timestamp = sample.time.toKotlinInstant(),
                        bpm = sample.beatsPerMinute.toInt(),
                        device = record.metadata.device?.model
                            ?: record.metadata.device?.manufacturer,
                        sourceApp = record.metadata.dataOrigin.packageName
                    )
                }
            }

            Metric.STEPS -> client.readRecords(
                ReadRecordsRequest(
                    StepsRecord::class,
                    range
                )
            ).records.map { record ->
                HealthConnectSample.Steps(
                    timestamp = record.endTime.toKotlinInstant(),
                    count = record.count,
                    start = record.startTime.toKotlinInstant(),
                    end = record.endTime.toKotlinInstant(),
                    device = record.metadata.device?.model ?: record.metadata.device?.manufacturer,
                    sourceApp = record.metadata.dataOrigin.packageName
                )
            }

            // Distance has no per-interval HealthConnectSample representation - it is only ever
            // summed for a window via readTotalDistance, matched into the steps daily aggregate.
            Metric.DISTANCE -> emptyList()
        }
    }

    /** Total distance for [from, to), used to enrich the steps daily aggregate. */
    suspend fun readTotalDistance(context: Context, from: Instant, to: Instant): Double? {
        val client = client(context) ?: return null
        val range = TimeRangeFilter.between(from.toJavaInstant(), to.toJavaInstant())
        val records = client.readRecords(ReadRecordsRequest(DistanceRecord::class, range)).records
        if (records.isEmpty()) return null
        return records.sumOf { it.distance.inMeters }
    }

    private fun client(context: Context): HealthConnectClient? {
        val applicationContext = context.applicationContext

        return if (isHealthConnectAvailable(applicationContext)) {
            HealthConnectClient.getOrCreate(applicationContext)
        } else {
            null
        }
    }

    private fun promptInstall(context: Context) {
        AlertController.openAlertDialog(
            AlertDialogModel(
                title = StringDesc.Resource(
                    SharedRes.strings.health_connect_not_installed_title
                ),
                message = StringDesc.Resource(
                    SharedRes.strings.health_connect_not_installed_message
                ),
                confirmLabel = StringDesc.Resource(
                    SharedRes.strings.health_connect_install_button
                ),
                onConfirm = {
                    val uriString =
                        "market://details?id=$PROVIDER_PACKAGE_NAME&url=healthconnect%3A%2F%2Fonboarding"

                    context.startActivity(
                        Intent(Intent.ACTION_VIEW).apply {
                            setPackage("com.android.vending")
                            data = uriString.toUri()
                            putExtra("overlay", true)
                            putExtra("callerId", context.packageName)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    )
                }
            )
        )
    }
}