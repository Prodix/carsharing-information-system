package com.syndicate.carsharing.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.sql.Time
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

class TimeDeserializer(vc: Class<Any?>?) : StdDeserializer<OffsetTime>(vc) {

    constructor() : this(null)

    override fun deserialize(p0: JsonParser?, p1: DeserializationContext?): OffsetTime {

        val date = p0!!.getValueAsString("")

        return OffsetTime.parse(date, DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_TIME)
            .toFormatter().withZone(ZoneOffset.UTC)
        )

    }
}