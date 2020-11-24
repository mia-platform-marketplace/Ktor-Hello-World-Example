package eu.miaplatform.service.model.response

import com.fasterxml.jackson.annotation.JsonProperty

data class HelloWorldResponse (
    @JsonProperty("userTokenSent")
    @get:JsonProperty("userToken")
    val token: String?,

    @JsonProperty("pathParamSent")
    @get:JsonProperty("pathParamSent")
    val pathParam: String?,

    @JsonProperty("queryParamSent")
    @get:JsonProperty("queryParamSent")
    val queryParam: String?,

    @JsonProperty("helloWorld")
    @get:JsonProperty("helloWorld")
    val helloWorld: String?
)