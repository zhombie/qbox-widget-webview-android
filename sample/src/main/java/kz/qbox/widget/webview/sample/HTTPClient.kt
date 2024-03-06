package kz.qbox.widget.webview.sample

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object HTTPClient {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
        install(Logging) {
            level = LogLevel.ALL
        }
    }

    @Serializable
    data class GenerateTokenParams(
        val caller: String,
        val dest: String
    )

    @Serializable
    data class GenerateTokenResponse(
        val token: String
    )

    suspend fun generateToken(params: GenerateTokenParams): GenerateTokenResponse? {
        val response = client.post(
            "https://${BuildConfig.API_BASE_URL}/api/generate"
        ) {
            contentType(ContentType.Application.Json)
            setBody(params)
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body()
        }

        return null
    }

    fun close() {
        client.close()
    }

}