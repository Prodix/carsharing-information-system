package com.syndicate.carsharing.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syndicate.carsharing.UserStore
import com.syndicate.carsharing.database.HttpClient
import com.syndicate.carsharing.database.models.Card
import com.syndicate.carsharing.database.models.RentHistory
import com.syndicate.carsharing.database.models.Transport
import com.syndicate.carsharing.models.CardModel
import com.syndicate.carsharing.models.HistoryModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class CardViewModel @Inject constructor(
    val userStore: UserStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(CardModel())
    val uiState = _uiState.asStateFlow()

    init {
        updateCards()
    }

    fun updateCardNumber(cardNumber: String) {
        _uiState.update {
            it.copy(cardNumber = cardNumber)
        }
    }

    fun updateCvc(cvc: String) {
        _uiState.update {
            it.copy(cvc = cvc)
        }
    }

    fun updateDate(date: LocalDate) {
        _uiState.update {
            it.copy(expireDate = date)
        }
    }

    fun updateMoney(amount: Int) {
        _uiState.update {
            it.copy(money = amount)
        }
    }

    fun updateCards() {
        viewModelScope.launch(Dispatchers.IO) {
            val token = userStore.getToken().first()
            val user = userStore.getUser().first()
            val cards = HttpClient.client.get(
                "${HttpClient.url}/account/card/get?id=${user.id}"
            ) {
                headers["Authorization"] = "Bearer $token"
            }.body<List<Card>>()

            _uiState.update {
                it.copy(
                    cards = cards
                )
            }
        }
    }
}