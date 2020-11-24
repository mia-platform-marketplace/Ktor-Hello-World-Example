package eu.miaplatform.service.model.request

import com.fasterxml.jackson.annotation.JsonProperty

class HelloWorldRequestBody (
    @JsonProperty("name")
    @get:JsonProperty("name")
    val name: String,

    @JsonProperty("surname")
    @get:JsonProperty("surname")
    val surname: String
)