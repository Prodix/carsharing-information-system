package com.syndicate.carsharing.database.models

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer
import com.syndicate.carsharing.json.DateTimeDeserializer
import java.time.OffsetDateTime

data class TransportLog (
    @JsonProperty("Id") val id: Int,
    @JsonProperty("UserId") val userId: Int,
    @JsonProperty("TransportId") val transportId: Int,
    @JsonProperty("Action") val action: String,
    @JsonProperty("DateTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    @JsonDeserialize(using = DateTimeDeserializer::class)
    @JsonSerialize(using = OffsetDateTimeSerializer::class)
    val dateTime: OffsetDateTime,
    @JsonProperty("RateId") val rateId: Int
)