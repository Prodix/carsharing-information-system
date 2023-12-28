package com.syndicate.carsharing.database.models

import com.fasterxml.jackson.annotation.JsonProperty

data class Car (
    @JsonProperty("id") val id: Int?,
    @JsonProperty("brand") val brand: String?,
    @JsonProperty("model") val model: String?,
    @JsonProperty("image_path") val imagePath: String?,
    @JsonProperty("fuel_level") val fuelLevel: String?,
    @JsonProperty("car_plate") val carPlate: String?,
    @JsonProperty("class") val category: Int?,
    @JsonProperty("params") val params: String?,
    @JsonProperty("insurance") val insurance: Int?,
    @JsonProperty("rate") val rate: Int?,
    @JsonProperty("address") val address: String?,
    @JsonProperty("reservation") val reservation: Int?
)