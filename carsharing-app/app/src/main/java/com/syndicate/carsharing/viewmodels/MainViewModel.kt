package com.syndicate.carsharing.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import com.syndicate.carsharing.models.MainModel
import com.syndicate.carsharing.models.Tag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainModel(null))
    val uiState = _uiState.asStateFlow()

    fun addTags(tags: List<Tag>) {
        _uiState.value = MainModel(tags)
    }
}