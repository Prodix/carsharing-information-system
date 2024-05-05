package com.syndicate.carsharing.database.models

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonProperty

data class Penalty (
    @JsonProperty("Id") val id: Int,
    @JsonProperty("UserId") val userId: Int,
    @JsonProperty("RelatedRent") val relatedRent: Int,
    @JsonProperty("RatingPenalty") val ratingPenalty: Int,
    @JsonProperty("Price") val price: Double,
    @JsonProperty("Description") val description: String,
    @JsonProperty("IsPaid") val isPaid: Boolean,
)