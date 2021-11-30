package eu.miaplatform.applications.helloworld

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import eu.miaplatform.baseModule
import eu.miaplatform.core.ktor.install
import eu.miaplatform.model.ErrorResponse
import eu.miaplatform.model.request.HelloWorldRequestBody
import eu.miaplatform.model.response.HelloWorldResponse
import eu.miaplatform.services.HelloWorldService
import io.kotest.core.spec.style.DescribeSpec
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk

/**
 * End-to-end tests.
 * You could test separately endpoints and logic in two different test classes.
 */
class HelloWorldApplicationTest : DescribeSpec({

    val objectMapper = ObjectMapper().apply {
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }

    val service = mockk<HelloWorldService>()

    beforeEach {
        clearAllMocks()
    }

    describe("/hello") {
        describe("get") {
            it("should return success object message") {
                withTestApplication({
                    baseModule()
                    install(HelloWorldApplication("", service))
                }) {
                    handleRequest(HttpMethod.Get, "/hello") {
                    }.apply {
                        assertThat(response.status()?.value).isEqualTo(HttpStatusCode.OK.value)

                        val expectedBody = objectMapper.writeValueAsString(
                            HelloWorldResponse(
                                null,
                                null,
                                "Hello world!"
                            )
                        )
                        assertThat(response.content).isEqualTo(expectedBody)
                    }
                }
            }

            it("should return success object message with query parameter") {
                withTestApplication({
                    baseModule()
                    install(HelloWorldApplication("", service))
                }) {
                    handleRequest(HttpMethod.Get, "/hello?queryParam=param") {
                    }.apply {
                        assertThat(response.status()?.value).isEqualTo(HttpStatusCode.OK.value)

                        val expectedBody = objectMapper.writeValueAsString(
                            HelloWorldResponse(
                                null,
                                "param",
                                "Hello world!"
                            )
                        )
                        assertThat(response.content).isEqualTo(expectedBody)
                    }
                }
            }
        }

        describe("/{pathParam}") {
            describe("post") {
                it("should return success object message with path param") {
                    withTestApplication({
                        baseModule()
                        install(HelloWorldApplication("", service))
                    }) {
                        val body = objectMapper.writeValueAsString(
                            HelloWorldRequestBody("name", "surname")
                        )

                        handleRequest(HttpMethod.Post, "/hello/1234") {
                            addHeader("Content-Type", "application/json")
                            setBody(body)
                        }.apply {
                            assertThat(response.status()?.value).isEqualTo(HttpStatusCode.OK.value)

                            val expectedBody = objectMapper.writeValueAsString(
                                HelloWorldResponse(
                                    "1234",
                                    null,
                                    "Hello world name surname!"
                                )
                            )
                            assertThat(response.content).isEqualTo(expectedBody)
                        }
                    }
                }

                it("should return bad request if body is malformed") {
                    withTestApplication({
                        baseModule()
                        install(HelloWorldApplication("", service))
                    }) {
                        val body = objectMapper.writeValueAsString(
                            mapOf("name" to "name")
                        )

                        handleRequest(HttpMethod.Post, "/hello/1234") {
                            addHeader("Content-Type", "application/json")
                            setBody(body)
                        }.apply {
                            assertThat(response.status()?.value).isEqualTo(HttpStatusCode.BadRequest.value)
                            val servicePackage = "eu.miaplatform"
                            val expectedBody = objectMapper.writeValueAsString(
                                // if you change the package name, remember to update it in the following error or the test will fail
                                ErrorResponse(1000, "Instantiation of [simple type, class $servicePackage.model.request.HelloWorldRequestBody] value failed for JSON property surname due to missing (therefore NULL) value for creator parameter surname which is a non-nullable type\n at [Source: (InputStreamReader); line: 1, column: 15] (through reference chain: $servicePackage.model.request.HelloWorldRequestBody[\"surname\"])")
                            )
                            assertThat(response.content).isEqualTo(expectedBody)
                        }
                    }
                }
            }
        }

        describe("/with-call") {
            describe("get") {
                it("should return success object message") {
                    coEvery { service.getBooksByHeaders(mapOf()) }
                        .returns(listOf("book1", "book2"))

                    withTestApplication({
                        baseModule()
                        install(HelloWorldApplication("", service))
                    }) {
                        handleRequest(HttpMethod.Get, "/hello/with-call") {
                        }.apply {
                            assertThat(response.status()?.value).isEqualTo(HttpStatusCode.OK.value)

                            val expectedBody = objectMapper.writeValueAsString(
                                HelloWorldResponse(
                                    null,
                                    null,
                                    "Hello world! Book list: book1, book2"
                                )
                            )
                            assertThat(response.content).isEqualTo(expectedBody)
                        }
                    }
                }

                it("should return error if service call fails") {
                    coEvery { service.getBooksByHeaders(mapOf()) }
                        .throws(Exception("some error occurred"))

                    withTestApplication({
                        baseModule()
                        install(HelloWorldApplication("", service))
                    }) {
                        handleRequest(HttpMethod.Get, "/hello/with-call") {
                        }.apply {
                            assertThat(response.status()?.value).isEqualTo(HttpStatusCode.InternalServerError.value)

                            val expectedBody = objectMapper.writeValueAsString(
                                ErrorResponse(1000, "some error occurred")
                            )
                            assertThat(response.content).isEqualTo(expectedBody)
                        }
                    }
                }

                it("should return success object message with query parameter") {
                    coEvery { service.getBooksByHeaders(mapOf()) }
                        .returns(listOf("book1", "book2"))

                    withTestApplication({
                        baseModule()
                        install(HelloWorldApplication("", service))
                    }) {
                        handleRequest(HttpMethod.Get, "/hello/with-call?queryParam=param") {
                        }.apply {
                            assertThat(response.status()?.value).isEqualTo(HttpStatusCode.OK.value)

                            val expectedBody = objectMapper.writeValueAsString(
                                HelloWorldResponse(
                                    null,
                                    "param",
                                    "Hello world! Book list: book1, book2"
                                )
                            )
                            assertThat(response.content).isEqualTo(expectedBody)
                        }
                    }
                }
            }
        }
    }
})