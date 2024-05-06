package com.syndicate.carsharing.database.models

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer
import com.syndicate.carsharing.json.DateDeserializer
import java.time.OffsetDateTime

data class Card(
    @JsonProperty("Id") val id: Int = 0,
    @JsonProperty("UserId") val userId: Int = 0,
    @JsonProperty("CardNumber") val cardNumber: String = "",
    @JsonProperty("Cvc") val cvc: String = "",
    @JsonDeserialize(using = DateDeserializer::class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonSerialize(using = OffsetDateTimeSerializer::class)
    @JsonProperty("ExpireDate") val expireDate: OffsetDateTime = OffsetDateTime.MIN,
)