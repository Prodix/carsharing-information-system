package com.syndicate.carsharing.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

class DateDeserializer(vc: Class<Any?>?) : StdDeserializer<OffsetDateTime>(vc) {

    constructor() : this(null)

    override fun deserialize(p0: JsonParser?, p1: DeserializationContext?): OffsetDateTime {

        val date = p0!!.getValueAsString("")

        return OffsetDateTime.parse(date, DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .appendOffset("+HH:mm", "+00:00")
            .toFormatter()
        ).withOffsetSameInstant(ZoneOffset.UTC)

    }

}