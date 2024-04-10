/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */
package io.redlink.more.more_app_mutliplatform.services.network.openapi.infrastructure

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement
import io.ktor.serialization.kotlinx.json.json
import io.ktor.http.*
import io.ktor.http.content.PartData
import io.realm.kotlin.internal.platform.WeakReference
import kotlin.Unit
import kotlinx.serialization.json.Json

import io.redlink.more.more_app_mutliplatform.services.network.openapi.auth.Authentication
import io.redlink.more.more_app_mutliplatform.services.network.openapi.auth.HttpBasicAuth

open class ApiClient(
        private val baseUrl: String,
        httpClientEngine: HttpClientEngine?,
        httpClientConfig: ((HttpClientConfig<*>) -> Unit)? = null,
        private val jsonBlock: Json
) {

    private val clientConfig: (HttpClientConfig<*>) -> Unit by lazy {
        {
            it.install(ContentNegotiation) { json(jsonBlock) }
            httpClientConfig?.invoke(it)
        }
    }

    private val client: HttpClient by lazy {
        httpClientEngine?.let { HttpClient(it, clientConfig) } ?: HttpClient(clientConfig)
    }

    private val authentications: kotlin.collections.Map<String, Authentication> by lazy {
        mapOf(
                "apiKey" to HttpBasicAuth()
        )
    }

    companion object {
        const val BASE_URL = "/api/v1"
        val JSON_DEFAULT = Json {
          ignoreUnknownKeys = true
          prettyPrint = true
          isLenient = true
        }
        protected val UNSAFE_HEADERS = listOf(HttpHeaders.ContentType)
    }

    /**
     * Set the username for the first HTTP basic authentication.
     *
     * @param username Username
     */
    fun setUsername(username: String) {
        val auth = authentications?.values?.firstOrNull { it is HttpBasicAuth } as HttpBasicAuth?
                ?: throw Exception("No HTTP basic authentication configured")
        auth.username = username
    }

    /**
     * Set the password for the first HTTP basic authentication.
     *
     * @param password Password
     */
    fun setPassword(password: String) {
        val auth = authentications?.values?.firstOrNull { it is HttpBasicAuth } as HttpBasicAuth?
                ?: throw Exception("No HTTP basic authentication configured")
        auth.password = password
    }

    protected suspend fun <T: Any?> multipartFormRequest(requestConfig: RequestConfig<T>, body: List<PartData>?, authNames: List<String>): HttpResponse? {
        return request(requestConfig, MultiPartFormDataContent(body ?: listOf()), authNames)
    }

    protected suspend fun <T: Any?> urlEncodedFormRequest(requestConfig: RequestConfig<T>, body: Parameters?, authNames: List<String>): HttpResponse? {
        return request(requestConfig, FormDataContent(body ?: Parameters.Empty), authNames)
    }

    protected suspend fun <T: Any?> jsonRequest(requestConfig: RequestConfig<T>, body: Any? = null, authNames: List<String>): HttpResponse? = request(requestConfig, body, authNames)

    protected suspend fun <T: Any?> request(requestConfig: RequestConfig<T>, body: Any? = null, authNames: List<String>): HttpResponse? {
        requestConfig.updateForAuth(authNames)
        val headers = requestConfig.headers
        val response = WeakReference(client.request {
            this.url {
                this.takeFrom(URLBuilder(baseUrl))
                appendPath(requestConfig.path.trimStart('/').split('/'))
                requestConfig.query.forEach { query ->
                    query.value.forEach { value ->
                        parameter(query.key, value)
                    }
                }
            }
            this.method = requestConfig.method.httpMethod
            headers.filter { header -> !UNSAFE_HEADERS.contains(header.key) }.forEach { header -> this.header(header.key, header.value) }
            if (requestConfig.method in listOf(
                    RequestMethod.PUT,
                    RequestMethod.POST
            )){
                this.contentType(ContentType.Application.Json)
            }
            if (requestConfig.method in listOf(
                    RequestMethod.PUT,
                    RequestMethod.POST,
                    RequestMethod.PATCH
                )) {
                this.setBody(body)
            }
        })
        return response.get()
    }

    private fun <T: Any?> RequestConfig<T>.updateForAuth(authNames: List<String>) {
        for (authName in authNames) {
            val auth = authentications?.get(authName) ?: throw Exception("Authentication undefined: $authName")
            auth.apply(query, headers)
        }
    }

    private fun URLBuilder.appendPath(components: List<String>): URLBuilder = apply {
        encodedPath = encodedPath.trimEnd('/') + components.joinToString("/", prefix = "/") { it.encodeURLQueryComponent() }
    }

    private val RequestMethod.httpMethod: HttpMethod
        get() = when (this) {
            RequestMethod.DELETE -> HttpMethod.Delete
            RequestMethod.GET -> HttpMethod.Get
            RequestMethod.HEAD -> HttpMethod.Head
            RequestMethod.PATCH -> HttpMethod.Patch
            RequestMethod.PUT -> HttpMethod.Put
            RequestMethod.POST -> HttpMethod.Post
            RequestMethod.OPTIONS -> HttpMethod.Options
        }
}