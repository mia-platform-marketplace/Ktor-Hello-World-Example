package eu.miaplatform.service

import com.fasterxml.jackson.databind.ObjectMapper
import eu.miaplatform.service.client.CrudClientInterface
import eu.miaplatform.service.client.HeadersToProxy
import eu.miaplatform.service.client.RetrofitClient
import eu.miaplatform.service.model.HealthBodyResponse
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import org.junit.Test
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import okhttp3.logging.HttpLoggingInterceptor
import org.slf4j.event.Level
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HealthTest {
    private val objectMapper = ObjectMapper()

    private val crudClient = RetrofitClient("http://crud-url", HttpLoggingInterceptor.Level.NONE, CrudClientInterface::class.java)

    @Test
    @KtorExperimentalAPI
    fun `Health should return OK`() {
        withTestApplication({
            module (
                logLevel = Level.DEBUG,
                crudClient = crudClient,
                headersToProxy = HeadersToProxy()
            )
        }) {
            handleRequest(HttpMethod.Get, "/-/healthz") {

            }.apply {
                assertTrue { response.status()?.value == HttpStatusCode.OK.value }

                val version = StatusService().getVersion()
                val body = objectMapper.readValue(response.content, HealthBodyResponse::class.java)
                val expectedRes = HealthBodyResponse("service-api", version, HealthBodyResponse.Status.OK.value)
                assertEquals(expectedRes, body)
            }
        }
    }

    @Test
    @KtorExperimentalAPI
    fun `Ready should return OK`() {
        withTestApplication({
            module (
                logLevel = Level.DEBUG,
                crudClient = crudClient,
                headersToProxy = HeadersToProxy()
            )
        }) {
            handleRequest(HttpMethod.Get, "/-/ready") {

            }.apply {
                assertTrue { response.status()?.value == HttpStatusCode.OK.value }

                val version = StatusService().getVersion()
                val body = objectMapper.readValue(response.content, HealthBodyResponse::class.java)
                val expectedRes = HealthBodyResponse("service-api", version, HealthBodyResponse.Status.OK.value)
                assertEquals(expectedRes, body)
            }
        }

    }

    @Test
    @KtorExperimentalAPI
    fun `Check Up should return OK`() {
        withTestApplication({
            module (
                logLevel = Level.DEBUG,
                crudClient = crudClient,
                headersToProxy = HeadersToProxy()
            )
        }) {
            handleRequest(HttpMethod.Get, "/-/check-up") {

            }.apply {
                assertTrue { response.status()?.value == HttpStatusCode.OK.value }

                val version = StatusService().getVersion()
                val body = objectMapper.readValue(response.content, HealthBodyResponse::class.java)
                val expectedRes = HealthBodyResponse("service-api", version, HealthBodyResponse.Status.OK.value)
                assertEquals(expectedRes, body)
            }
        }

    }
}