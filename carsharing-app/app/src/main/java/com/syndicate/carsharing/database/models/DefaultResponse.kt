package com.syndicate.carsharing.database.models

import com.fasterxml.jackson.annotation.JsonProperty

data class DefaultResponse (
    @JsonProperty("token") val token: String? = "",
    @JsonProperty("message") val message: String? = "",
    @JsonProperty("status_code") val status_code: Int? = 0
)