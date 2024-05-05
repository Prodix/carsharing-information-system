package com.syndicate.carsharing.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syndicate.carsharing.UserStore
import com.syndicate.carsharing.database.HttpClient
import com.syndicate.carsharing.database.models.Penalty
import com.syndicate.carsharing.database.models.RentHistory
import com.syndicate.carsharing.database.models.Transport
import com.syndicate.carsharing.database.models.TransportLog
import com.syndicate.carsharing.models.HistoryModel
import com.syndicate.carsharing.models.PenaltyModel
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
class PenaltyViewModel @Inject constructor(
    val userStore: UserStore,
    val mainViewModel: MainViewModel
) : ViewModel() {
    private val _uiState = MutableStateFlow(PenaltyModel())
    val uiState = _uiState.asStateFlow()

    init {
        updatePenalty()
    }

    fun updatePenalty() {
        viewModelScope.launch(Dispatchers.IO) {
            val token = userStore.getToken().first()
            val user = userStore.getUser().first()
            val penalty = HttpClient.client.get(
                "${HttpClient.url}/account/penalty/get?id=${user.id}"
            ) {
                headers["Authorization"] = "Bearer $token"
            }.body<List<Penalty>>()

            _uiState.update {
                it.copy(
                    penalty = penalty
                )
            }
        }
    }
}