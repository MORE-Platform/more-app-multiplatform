/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.app.android.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import io.redlink.more.app.android.MoreApplication
import org.json.JSONArray

object AlarmUtils {
    private const val TAG_ALARMS = ":alarms"

    fun addAlarm(
        context: Context,
        intent: Intent,
        notificationId: String,
        triggerAtMillis: Long
    ) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                // Fallback: schedule inexact (OS may batch). Consider requesting SCHEDULE_EXACT_ALARM if exact timing is required.
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }

        saveAlarmId(context, notificationId.hashCode())
    }

    fun cancelAlarm(context: Context, intent: Intent, notificationId: Int) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()

        removeAlarmId(context, notificationId)
    }

    fun cancelAllAlarms(context: Context, intent: Intent) {
        getAlarmIds(context).forEach { idAlarm ->
            cancelAlarm(context, intent, idAlarm)
        }
    }

    fun hasAlarm(context: Context, intent: Intent, notificationId: String): Boolean {
        val pi = PendingIntent.getBroadcast(
            context,
            notificationId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        return pi != null
    }

    private fun saveAlarmId(context: Context, id: Int) {
        val idsAlarms = getAlarmIds(context).toMutableList()
        if (idsAlarms.contains(id)) return

        idsAlarms.add(id)
        saveIdsInPreferences(context, idsAlarms)
    }

    fun removeAlarmId(context: Context, id: Int) {
        val idsAlarms = getAlarmIds(context).toMutableList()
        idsAlarms.removeAll { it == id }
        saveIdsInPreferences(context, idsAlarms)
    }

    private fun getAlarmIds(context: Context): List<Int> {
        return try {
            val prefs = MoreApplication.shared!!.sharedStorageRepository
            val key = context.packageName + TAG_ALARMS
            val json = prefs.load(key, "[]")
            val jsonArray = JSONArray(json)

            buildList {
                for (i in 0 until jsonArray.length()) {
                    add(jsonArray.getInt(i))
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveIdsInPreferences(context: Context, ids: List<Int>) {
        val jsonArray = JSONArray()
        ids.forEach { idAlarm -> jsonArray.put(idAlarm) }

        val prefs = MoreApplication.shared!!.sharedStorageRepository
        prefs.store(context.packageName + TAG_ALARMS, jsonArray.toString())
    }
}