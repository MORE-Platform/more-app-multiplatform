package io.redlink.more.more_app_mutliplatform.services.network.openapi.infrastructure

import io.ktor.http.Headers
import io.ktor.http.isSuccess
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.typeInfo
import io.realm.kotlin.internal.platform.WeakReference
import io.realm.kotlin.internal.platform.freeze

open class HttpResponse<T : Any>(val response: io.ktor.client.statement.HttpResponse, val provider: BodyProvider<T>) {
    val status: Int = response.status.value
    val success: Boolean = response.status.isSuccess()
    val headers: Map<String, List<String>> = response.headers.mapEntries().freeze()
    suspend fun body():WeakReference<T> = WeakReference(provider.body(response).freeze())
    suspend fun <V : Any> typedBody(type: TypeInfo): V = provider.typedBody<V>(response, type).freeze()

    companion object {
        private fun Headers.mapEntries(): Map<String, List<String>> {
            val result = mutableMapOf<String, List<String>>()
            entries().forEach { result[it.key] = it.value }
            return result
        }
    }
}

interface BodyProvider<T : Any> {
    suspend fun body(response: io.ktor.client.statement.HttpResponse): T
    suspend fun <V : Any> typedBody(response: io.ktor.client.statement.HttpResponse, type: TypeInfo): V
}

class TypedBodyProvider<T : Any>(private val type: TypeInfo) : BodyProvider<T> {
    @Suppress("UNCHECKED_CAST")
    override suspend fun body(response: io.ktor.client.statement.HttpResponse): T =
            response.call.body(type).freeze() as T

    @Suppress("UNCHECKED_CAST")
    override suspend fun <V : Any> typedBody(response: io.ktor.client.statement.HttpResponse, type: TypeInfo): V =
            response.call.body(type) as V
}

class MappedBodyProvider<S : Any, T : Any>(private val provider: BodyProvider<S>, private val block: S.() -> T) : BodyProvider<T> {
    override suspend fun body(response: io.ktor.client.statement.HttpResponse): T =
            block(provider.body(response).freeze())

    override suspend fun <V : Any> typedBody(response: io.ktor.client.statement.HttpResponse, type: TypeInfo): V =
            provider.typedBody<V>(response, type).freeze()
}

inline fun <reified T : Any> io.ktor.client.statement.HttpResponse.wrap(): HttpResponse<T> =
        HttpResponse(this, TypedBodyProvider<T>(typeInfo<T>()).freeze())

fun <T : Any, V : Any> HttpResponse<T>.map(block: T.() -> V): HttpResponse<V> =
        HttpResponse(response, MappedBodyProvider(provider, block).freeze())
