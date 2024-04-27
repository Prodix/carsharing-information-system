package com.syndicate.carsharing.database.models

import com.fasterxml.jackson.annotation.JsonProperty

data class Rate (
    @JsonProperty("Id") val id: Int = 0,
    @JsonProperty("TransportId") val transportId: Int = 0,
    @JsonProperty("RateName") val rateName: String = "",
    @JsonProperty("OnRoadPrice") val onRoadPrice: Double = 0.0,
    @JsonProperty("ParkingPrice") val parkingPrice: Double = 0.0
)