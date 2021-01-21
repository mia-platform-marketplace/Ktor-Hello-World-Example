package eu.miaplatform.service.model.request

import com.papsign.ktor.openapigen.annotations.parameters.HeaderParam
import com.papsign.ktor.openapigen.annotations.parameters.PathParam

data class HelloWorldPostRequest (
    @PathParam("Description of the param")
    val pathParam: String
)