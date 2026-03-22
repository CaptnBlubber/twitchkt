package io.github.captnblubber.twitchkt.helix.internal

import io.github.captnblubber.twitchkt.TwitchKtConfig
import io.github.captnblubber.twitchkt.auth.TwitchScope
import io.github.captnblubber.twitchkt.error.TwitchApiException
import io.github.captnblubber.twitchkt.error.mapTwitchApiError
import io.github.captnblubber.twitchkt.helix.Page
import io.github.captnblubber.twitchkt.logging.LogLevel
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal class HelixHttpClient(
    private val httpClient: HttpClient,
    private val config: TwitchKtConfig,
) {
    @PublishedApi
    internal val json: Json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
        }

    @PublishedApi
    internal inline fun <reified T> encodeBody(value: T): TextContent = TextContent(json.encodeToString(value), ContentType.Application.Json)

    suspend inline fun <reified T> get(
        endpoint: String,
        params: List<Pair<String, String>> = emptyList(),
    ): TwitchResponse<T> {
        val token = config.tokenProvider.token()
        val response =
            httpClient.get("${config.helixBaseUrl}/$endpoint") {
                header("Authorization", "Bearer $token")
                header("Client-Id", config.clientId)
                params.forEach { (key, value) -> url.parameters.append(key, value) }
            }
        return handleResponse(response)
    }

    suspend inline fun <reified T> post(
        endpoint: String,
        body: TextContent? = null,
        params: List<Pair<String, String>> = emptyList(),
    ): TwitchResponse<T> {
        val token = config.tokenProvider.token()
        val response =
            httpClient.post("${config.helixBaseUrl}/$endpoint") {
                header("Authorization", "Bearer $token")
                header("Client-Id", config.clientId)
                params.forEach { (key, value) -> url.parameters.append(key, value) }
                if (body != null) {
                    setBody(body)
                }
            }
        return handleResponse(response)
    }

    suspend fun postNoContent(
        endpoint: String,
        body: TextContent? = null,
        params: List<Pair<String, String>> = emptyList(),
    ) {
        val token = config.tokenProvider.token()
        val response =
            httpClient.post("${config.helixBaseUrl}/$endpoint") {
                header("Authorization", "Bearer $token")
                header("Client-Id", config.clientId)
                params.forEach { (key, value) -> url.parameters.append(key, value) }
                if (body != null) {
                    setBody(body)
                }
            }
        handleNoContentResponse(response)
    }

    suspend fun patchNoContent(
        endpoint: String,
        body: TextContent? = null,
        params: List<Pair<String, String>> = emptyList(),
    ) {
        val token = config.tokenProvider.token()
        val response =
            httpClient.patch("${config.helixBaseUrl}/$endpoint") {
                header("Authorization", "Bearer $token")
                header("Client-Id", config.clientId)
                params.forEach { (key, value) -> url.parameters.append(key, value) }
                if (body != null) {
                    setBody(body)
                }
            }
        handleNoContentResponse(response)
    }

    suspend inline fun <reified T> patch(
        endpoint: String,
        body: TextContent? = null,
        params: List<Pair<String, String>> = emptyList(),
    ): TwitchResponse<T> {
        val token = config.tokenProvider.token()
        val response =
            httpClient.patch("${config.helixBaseUrl}/$endpoint") {
                header("Authorization", "Bearer $token")
                header("Client-Id", config.clientId)
                params.forEach { (key, value) -> url.parameters.append(key, value) }
                if (body != null) {
                    setBody(body)
                }
            }
        return handleResponse(response)
    }

    suspend inline fun <reified T> put(
        endpoint: String,
        body: TextContent? = null,
        params: List<Pair<String, String>> = emptyList(),
    ): TwitchResponse<T> {
        val token = config.tokenProvider.token()
        val response =
            httpClient.put("${config.helixBaseUrl}/$endpoint") {
                header("Authorization", "Bearer $token")
                header("Client-Id", config.clientId)
                params.forEach { (key, value) -> url.parameters.append(key, value) }
                if (body != null) {
                    setBody(body)
                }
            }
        return handleResponse(response)
    }

    suspend fun putNoContent(
        endpoint: String,
        body: TextContent? = null,
        params: List<Pair<String, String>> = emptyList(),
    ) {
        val token = config.tokenProvider.token()
        val response =
            httpClient.put("${config.helixBaseUrl}/$endpoint") {
                header("Authorization", "Bearer $token")
                header("Client-Id", config.clientId)
                params.forEach { (key, value) -> url.parameters.append(key, value) }
                if (body != null) {
                    setBody(body)
                }
            }
        handleNoContentResponse(response)
    }

    suspend inline fun <reified R> getTyped(
        endpoint: String,
        params: List<Pair<String, String>> = emptyList(),
    ): R {
        val token = config.tokenProvider.token()
        val response =
            httpClient.get("${config.helixBaseUrl}/$endpoint") {
                header("Authorization", "Bearer $token")
                header("Client-Id", config.clientId)
                params.forEach { (key, value) -> url.parameters.append(key, value) }
            }
        return handleTypedResponse(response)
    }

    suspend inline fun <reified R> putTyped(
        endpoint: String,
        body: TextContent? = null,
        params: List<Pair<String, String>> = emptyList(),
    ): R {
        val token = config.tokenProvider.token()
        val response =
            httpClient.put("${config.helixBaseUrl}/$endpoint") {
                header("Authorization", "Bearer $token")
                header("Client-Id", config.clientId)
                params.forEach { (key, value) -> url.parameters.append(key, value) }
                if (body != null) {
                    setBody(body)
                }
            }
        return handleTypedResponse(response)
    }

    suspend inline fun <reified T> delete(
        endpoint: String,
        params: List<Pair<String, String>> = emptyList(),
    ): TwitchResponse<T> {
        val token = config.tokenProvider.token()
        val response =
            httpClient.delete("${config.helixBaseUrl}/$endpoint") {
                header("Authorization", "Bearer $token")
                header("Client-Id", config.clientId)
                params.forEach { (key, value) -> url.parameters.append(key, value) }
            }
        return handleResponse(response)
    }

    suspend fun deleteNoContent(
        endpoint: String,
        params: List<Pair<String, String>> = emptyList(),
    ) {
        val token = config.tokenProvider.token()
        val response =
            httpClient.delete("${config.helixBaseUrl}/$endpoint") {
                header("Authorization", "Bearer $token")
                header("Client-Id", config.clientId)
                params.forEach { (key, value) -> url.parameters.append(key, value) }
            }
        handleNoContentResponse(response)
    }

    inline fun <reified T> paginate(
        endpoint: String,
        params: List<Pair<String, String>> = emptyList(),
        pageSize: Int = 100,
    ): Flow<T> =
        flow {
            var cursor: String? = null
            do {
                val paginatedParams =
                    buildList {
                        addAll(params)
                        add("first" to pageSize.toString())
                        cursor?.let { add("after" to it) }
                    }
                val response = get<T>(endpoint, paginatedParams)
                response.data.forEach { emit(it) }
                cursor = response.pagination?.cursor
            } while (cursor != null)
        }

    /**
     * Fetches a single page of results from a paginated Helix endpoint.
     *
     * @param endpoint the Helix endpoint path (e.g. `"channels/followers"`).
     * @param params query parameters to include in the request.
     * @param pageSize the maximum number of items to return. When `null`, the Twitch API
     * uses its own default for the endpoint. Must be positive if provided.
     * @return a [Page] containing the items on this page and the cursor for the next page,
     * or `null` cursor if this is the last page.
     */
    suspend inline fun <reified T> getPage(
        endpoint: String,
        params: List<Pair<String, String>> = emptyList(),
        pageSize: Int? = null,
    ): Page<T> {
        require(pageSize == null || pageSize > 0) { "pageSize must be positive, was $pageSize" }
        val fullParams =
            buildList {
                addAll(params)
                pageSize?.let { add("first" to it.toString()) }
            }
        val response = get<T>(endpoint, fullParams)
        return Page(data = response.data, cursor = response.pagination?.cursor)
    }

    @PublishedApi
    internal suspend inline fun <reified T> handleResponse(response: HttpResponse): TwitchResponse<T> {
        if (response.status.value in 200..299) {
            return json.decodeFromString<TwitchResponse<T>>(response.bodyAsText())
        }
        throw mapException(response)
    }

    @PublishedApi
    internal suspend inline fun <reified R> handleTypedResponse(response: HttpResponse): R {
        if (response.status.value in 200..299) {
            return json.decodeFromString<R>(response.bodyAsText())
        }
        throw mapException(response)
    }

    @PublishedApi
    internal suspend fun handleNoContentResponse(response: HttpResponse) {
        if (response.status.value in 200..299) {
            return
        }
        throw mapException(response)
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun mapException(response: HttpResponse): TwitchApiException {
        val errorBody = response.bodyAsText()
        config.logger?.log(LogLevel.ERROR, TAG) {
            "Helix API error ${response.status.value}: $errorBody"
        }
        val retryAfterMs =
            if (response.status.value == 429) {
                val resetEpoch = response.headers["Ratelimit-Reset"]?.toLongOrNull() ?: 0L
                val nowEpoch = Clock.System.now().epochSeconds
                ((resetEpoch - nowEpoch) * 1000).coerceAtLeast(0L)
            } else {
                null
            }
        return mapTwitchApiError(response.status.value, errorBody, retryAfterMs)
    }

    suspend fun validateScopes(vararg required: TwitchScope) {
        val provider = config.scopeProvider ?: return
        val granted = provider.scopes()
        val missing = required.filterNot { TwitchScope.isSatisfied(it, granted) }
        if (missing.isNotEmpty()) {
            throw TwitchApiException.MissingScope(missing)
        }
    }

    suspend fun validateAnyScope(vararg anyOf: TwitchScope) {
        val provider = config.scopeProvider ?: return
        val granted = provider.scopes()
        val satisfied = anyOf.any { TwitchScope.isSatisfied(it, granted) }
        if (!satisfied) {
            throw TwitchApiException.MissingScope(anyOf.toList())
        }
    }

    companion object {
        private const val TAG = "HelixHttpClient"
    }
}
