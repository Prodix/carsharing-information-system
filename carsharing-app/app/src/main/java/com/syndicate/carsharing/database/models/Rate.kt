package com.syndicate.carsharing.database.models

import com.fasterxml.jackson.annotation.JsonProperty

data class Rate (
    @JsonProperty("Id") val id: Int,
    @JsonProperty("TransportId") val transportId: Int,
    @JsonProperty("RateName") val rateName: String,
    @JsonProperty("OnRoadPrice") val onRoadPrice: Double,
    @JsonProperty("ParkingPrice") val parkingPrice: Double
)