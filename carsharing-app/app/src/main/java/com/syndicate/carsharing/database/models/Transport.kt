package com.syndicate.carsharing.database.models

import com.fasterxml.jackson.annotation.JsonProperty

data class Transport (
    @JsonProperty("Id") val id: Int,
    @JsonProperty("TransportType") val transportType: String,
    @JsonProperty("CarName") val carName: String,
    @JsonProperty("CarNumber") val carNumber: String,
    @JsonProperty("CarImagePath") val carImagePath: String,
    @JsonProperty("IsReserved") val isReserved: Boolean,
    @JsonProperty("GasLevel") val gasLevel: Int,
    @JsonProperty("HasInsurance") val hasInsurance: Boolean,
    @JsonProperty("IsDoorOpened") val isDoorOpened: Boolean,
    @JsonProperty("Longitude") val longitude: Double,
    @JsonProperty("Latitude") val latitude: Double,
)