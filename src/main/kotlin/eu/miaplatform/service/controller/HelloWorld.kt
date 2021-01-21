package eu.miaplatform.service.controller

import com.papsign.ktor.openapigen.route.apiRouting
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import com.papsign.ktor.openapigen.route.tag
import eu.miaplatform.service.client.CrudClientInterface
import eu.miaplatform.service.client.HeadersToProxy
import eu.miaplatform.service.client.RetrofitClient
import eu.miaplatform.service.model.InternalServerErrorException
import eu.miaplatform.service.model.ServiceTag
import eu.miaplatform.service.model.UnauthorizedException
import eu.miaplatform.service.model.request.HelloWorldGetRequest
import eu.miaplatform.service.model.request.HelloWorldPostRequest
import eu.miaplatform.service.model.request.HelloWorldRequestBody
import eu.miaplatform.service.model.response.HelloWorldResponse
import io.ktor.application.Application
import kotlinx.coroutines.async

fun helloWorld(application: Application, crudClient: RetrofitClient<CrudClientInterface>, headersToProxy: HeadersToProxy) {

    application.apiRouting {
        route("/hello") {
            tag(ServiceTag) {
                get<HelloWorldGetRequest, HelloWorldResponse>(
                    info("The description of the endpoint")
                ) { params ->

                    val response = HelloWorldResponse(
                        null,
                        params.queryParam,
                        "Hello world!"
                    )

                    respond(response)
                }

                route("/{pathParam}").post<HelloWorldPostRequest, HelloWorldResponse, HelloWorldRequestBody>(
                    info("The description of the endpoint")
                ) { params, requestBody ->

                    val response = HelloWorldResponse(
                        params.pathParam,
                        null,
                        "Hello world ${requestBody.name} ${requestBody.surname}!"
                    )

                    respond(response)
                }

                route("/with-call").get<HelloWorldGetRequest, HelloWorldResponse>(
                    info("The description of the endpoint")
                ) { params ->

                    val headers = headersToProxy.proxy(this.pipeline.context)
                    val booksCall = application.async {
                        crudClient.getRestClient().getBooks(headers)
                    }

                    val books = try {
                        booksCall.await()
                    } catch (e: Exception) {
                        throw InternalServerErrorException(1002, "books call failed")
                    }

                    val response = HelloWorldResponse(
                        null,
                        params.queryParam,
                        "Hello world! Book list: ${books.joinToString()}"
                    )

                    respond(response)
                }
            }
        }
    }
}
