package com.syndicate.carsharing

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

fun fromUnixMilli(time: Long): OffsetDateTime {
    return OffsetDateTime.ofInstant(
        Instant.ofEpochMilli(
            time
        ), ZoneOffset.UTC
    )
}