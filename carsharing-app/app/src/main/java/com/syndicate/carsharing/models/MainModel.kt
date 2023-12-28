package com.syndicate.carsharing.models

import com.syndicate.carsharing.database.models.Car

data class MainModel (
    val search: String = "",
    val cars: List<Car> = listOf(),
    val filter: List<String> = listOf(),
    val startCar: List<Car> = listOf()
)