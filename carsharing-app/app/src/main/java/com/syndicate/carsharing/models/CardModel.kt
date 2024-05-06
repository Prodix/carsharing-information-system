package com.syndicate.carsharing.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.syndicate.carsharing.database.models.Card
import java.time.LocalDate

data class CardModel (
    val cards: List<Card> = emptyList(),
    val cardNumber: String = "",
    val cvc: String = "",
    val expireDate: LocalDate = LocalDate.now(),
    val money: Int = 0
)