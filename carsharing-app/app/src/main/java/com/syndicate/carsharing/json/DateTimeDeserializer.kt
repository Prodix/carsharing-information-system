package com.syndicate.carsharing.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

class DateTimeDeserializer(vc: Class<Any?>?) : StdDeserializer<OffsetDateTime>(vc) {

    constructor() : this(null)

    override fun deserialize(p0: JsonParser?, p1: DeserializationContext?): OffsetDateTime {

        val date = p0!!.getValueAsString("")

        return OffsetDateTime.parse(date, DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .appendFraction(ChronoField.MICRO_OF_SECOND, 0, 6, true)
            .appendOffset("+HH:mm", "")
            .toFormatter()
        )

    }

}