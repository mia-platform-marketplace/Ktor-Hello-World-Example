package eu.miaplatform.service

import ch.qos.logback.classic.util.ContextInitializer
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.papsign.ktor.openapigen.OpenAPIGen
import com.papsign.ktor.openapigen.interop.withAPI
import com.papsign.ktor.openapigen.route.apiRouting
import com.papsign.ktor.openapigen.schema.builder.provider.DefaultObjectSchemaProvider
import com.papsign.ktor.openapigen.schema.namer.DefaultSchemaNamer
import com.papsign.ktor.openapigen.schema.namer.SchemaNamer
import eu.miaplatform.service.client.CrudClientInterface
import eu.miaplatform.service.client.HeadersToProxy
import eu.miaplatform.service.client.RetrofitClient
import eu.miaplatform.service.controller.documentation
import eu.miaplatform.service.controller.health
import eu.miaplatform.service.controller.helloWorld
import eu.miaplatform.service.model.ErrorResponse
import eu.miaplatform.service.model.BadRequestException
import eu.miaplatform.service.model.InternalServerErrorException
import eu.miaplatform.service.model.NotFoundException
import eu.miaplatform.service.model.UnauthorizedException
import io.ktor.application.Application
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.application.install
import io.ktor.jackson.jackson
import io.ktor.request.*
import io.ktor.server.netty.*
import io.ktor.util.KtorExperimentalAPI
import okhttp3.logging.HttpLoggingInterceptor
import org.slf4j.event.Level
import java.lang.reflect.InvocationTargetException
import java.util.*
import kotlin.reflect.KType

fun main(args: Array<String>) {
    System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, System.getenv("LOG_CONFIG_FILE"))
    val timeZone = System.getenv("TIME_ZONE")
    if(timeZone != null) {
        TimeZone.setDefault(TimeZone.getTimeZone(timeZone))
    }
    EngineMain.main(args)
}

@KtorExperimentalAPI
fun Application.module() {

    val logLevel = when (environment.config.property("ktor.log.level").getString().toUpperCase()) {
        "DEBUG" -> Level.DEBUG
        "ERROR" -> Level.ERROR
        "TRACE" -> Level.TRACE
        "WARN" -> Level.WARN
        else -> Level.INFO
    }

    val httpLogLevel = when (environment.config.property("ktor.log.httpLogLevel").getString().toUpperCase()) {
        "BASIC" -> HttpLoggingInterceptor.Level.BASIC
        "BODY" -> HttpLoggingInterceptor.Level.BODY
        "HEADERS" -> HttpLoggingInterceptor.Level.HEADERS
        else -> HttpLoggingInterceptor.Level.NONE
    }

    val additionalHeadersToProxy = System.getenv("ADDITIONAL_HEADERS_TO_PROXY") ?: ""
    val headersToProxy = HeadersToProxy(additionalHeadersToProxy)

    val crudClient = RetrofitClient(basePath = "http://crud-url/", logLevel = httpLogLevel, clazz = CrudClientInterface::class.java)

    module(logLevel, crudClient, headersToProxy)
}

@KtorExperimentalAPI
fun Application.module(
    logLevel: Level,
    crudClient: RetrofitClient<CrudClientInterface>,
    headersToProxy: HeadersToProxy
) {

    install(CallLogging) {
        level = logLevel
        filter { call -> call.request.path().startsWith("/") }
    }

    // Documentation here: https://github.com/papsign/Ktor-OpenAPI-Generator
    val api = install(OpenAPIGen) {
        info {
            version = StatusService().getVersion()
            title = "Service name"
            description = "The service description"
            contact {
                name = "Name of the contact"
                email = "contact@email.com"
            }
        }
        server("https://test.host/") {
            description = "Test environment"
        }
        server("https://preprod.host/") {
            description = "Preproduction environment"
        }
        server("https://cloud.host/") {
            description = "Production environment"
        }
        replaceModule(DefaultSchemaNamer, object: SchemaNamer {
            val regex = Regex("[A-Za-z0-9_.]+")
            override fun get(type: KType): String {
                return type.toString().replace(regex) { it.value.split(".").last() }.replace(Regex(">|<|, "), "_")
            }
        })
        replaceModule(DefaultObjectSchemaProvider, CustomJacksonObjectSchemaProvider)
    }

    install(StatusPages) {
        withAPI(api) {
            exception<UnauthorizedException, ErrorResponse>(HttpStatusCode.Unauthorized) {
                ErrorResponse(it.code, it.errorMessage)
            }
            exception<NotFoundException, ErrorResponse>(HttpStatusCode.NotFound) {
                ErrorResponse(it.code, it.errorMessage)
            }
            exception<BadRequestException, ErrorResponse>(HttpStatusCode.BadRequest) {
                ErrorResponse(it.code, it.errorMessage)
            }
            exception<MissingKotlinParameterException, ErrorResponse>(HttpStatusCode.BadRequest) {
                ErrorResponse(1000, it.localizedMessage)
            }
            exception<InvocationTargetException, ErrorResponse>(HttpStatusCode.BadRequest) {
                ErrorResponse(1000, it.targetException.localizedMessage)
            }
            exception<InvalidFormatException, ErrorResponse>(HttpStatusCode.BadRequest) {
                ErrorResponse(1000, it.localizedMessage)
            }
            exception<InternalServerErrorException, ErrorResponse>(HttpStatusCode.InternalServerError) {
                ErrorResponse(it.code, it.errorMessage)
            }
            exception<Exception, ErrorResponse>(HttpStatusCode.InternalServerError) {
                ErrorResponse(1000, it.localizedMessage ?: "Generic error")
            }

        }
    }

    install(ContentNegotiation) {
        jackson {
            this.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
    }

    apiRouting {
        //here goes your controller
        helloWorld(this@module, crudClient, headersToProxy)
    }

    routing {
        health()
        documentation(this.application)
    }
}