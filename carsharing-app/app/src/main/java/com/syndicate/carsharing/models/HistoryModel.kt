package com.syndicate.carsharing.models

import com.syndicate.carsharing.database.models.RentHistory

data class HistoryModel (
    val history: List<Pair<String, RentHistory>> = emptyList()
)