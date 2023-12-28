package com.syndicate.carsharing.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.syndicate.carsharing.database.managers.CarManager
import com.syndicate.carsharing.database.models.Car
import com.syndicate.carsharing.models.MainModel
import com.syndicate.carsharing.models.SignInModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainModel())
    val uiState = _uiState.asStateFlow()

    init {
        CarManager.getCars(this)
    }

    fun search(query: String) {
        if (query == "") {
            _uiState.value = MainModel(query, _uiState.value.startCar, _uiState.value.filter, _uiState.value.startCar)
            return
        }
        val cars = _uiState.value.startCar.toList().filter { it.brand?.contains(query) == true || it.model?.contains(query) == true }
        _uiState.value = MainModel(query, cars, _uiState.value.filter, _uiState.value.startCar)
    }

    fun filter(params: List<String>) {

        if (params.isEmpty()) {
            _uiState.value = MainModel(_uiState.value.search, _uiState.value.startCar, params, _uiState.value.startCar)
            return
        }

        val cars = _uiState.value.startCar.filter { params.contains(it.fuelLevel) }
        _uiState.value = MainModel(_uiState.value.search, cars, params, _uiState.value.startCar)
    }

    fun changeStartCar(cars: List<Car>) {
        _uiState.value = MainModel(_uiState.value.search, cars, _uiState.value.filter, cars)
    }

    fun addToParams(value: String) {
        val list = _uiState.value.filter.toMutableList()
        list.add(value)
        filter(list.toList())
    }

    fun removeFromParams(value: String) {
        val list = _uiState.value.filter.toMutableList()
        list.remove(value)
        filter(list.toList())
    }
}