package com.syndicate.carsharing.database.models

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer
import com.syndicate.carsharing.json.DateDeserializer
import com.syndicate.carsharing.json.DateTimeDeserializer
import com.syndicate.carsharing.json.TimeDeserializer
import java.time.OffsetDateTime
import java.time.OffsetTime

data class RentHistory(
    @JsonProperty("Id") val id: Int,
    @JsonProperty("UserId") val userId: Int,
    @JsonProperty("TransportId") val transportId: Int,
    @JsonProperty("RateId") val rateId: Int,
    @JsonProperty("Price") val price: Double,
    @JsonDeserialize(using = DateDeserializer::class)
    @JsonSerialize(using = OffsetDateTimeSerializer::class)
    @JsonProperty("Date") val date: OffsetDateTime,
    @JsonProperty("RentTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ssXXX")
    @JsonDeserialize(using = TimeDeserializer::class)
    @JsonSerialize(using = OffsetDateTimeSerializer::class)
    val rentTime: OffsetTime
)