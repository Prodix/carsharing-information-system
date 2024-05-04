package com.syndicate.carsharing.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syndicate.carsharing.UserStore
import com.syndicate.carsharing.database.HttpClient
import com.syndicate.carsharing.database.models.RentHistory
import com.syndicate.carsharing.database.models.Transport
import com.syndicate.carsharing.database.models.TransportLog
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
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    val userStore: UserStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryModel())
    val uiState = _uiState.asStateFlow()

    init {
        updateHistory()
    }

    fun updateHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val token = userStore.getToken().first()
            val response = HttpClient.client.get(
                "${HttpClient.url}/account/rent_history/get"
            ) {
                headers["Authorization"] = "Bearer $token"
            }.body<List<RentHistory>>().sortedBy { x -> x.id }.reversed()

            val history: MutableList<Pair<String, RentHistory>> = mutableListOf()
            val transport = HttpClient.client.get(
                "${HttpClient.url}/transport/get"
            ) {
                headers["Authorization"] = "Bearer $token"
            }.body<List<Transport>>()

            for (rentHistory in response) {
                history.add(Pair(transport.first {x -> x.id == rentHistory.transportId}.carName, rentHistory))
            }

            _uiState.update {
                it.copy(
                    history = history.toList()
                )
            }
        }
    }
}