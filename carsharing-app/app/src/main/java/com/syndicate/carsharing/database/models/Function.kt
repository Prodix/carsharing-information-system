package com.syndicate.carsharing.database.models

import com.fasterxml.jackson.annotation.JsonProperty

data class Function (
    @JsonProperty("Id") val id: Int,
    @JsonProperty("TransportId") val transportId: String,
    @JsonProperty("FunctionData") val functionData: String
)