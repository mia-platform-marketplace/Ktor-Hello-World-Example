package eu.miaplatform.service.model.request

import com.papsign.ktor.openapigen.annotations.parameters.HeaderParam
import com.papsign.ktor.openapigen.annotations.parameters.QueryParam

data class HelloWorldGetRequest (
    @HeaderParam("Description of the header", explode = false)
    val token: String,

    @QueryParam("Description of the query param")
    val queryParam: String?
)