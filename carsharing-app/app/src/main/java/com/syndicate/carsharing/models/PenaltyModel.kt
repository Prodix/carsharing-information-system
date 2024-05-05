package com.syndicate.carsharing.models

import com.syndicate.carsharing.database.models.Penalty

data class PenaltyModel (
    val penalty: List<Penalty> = emptyList()
)