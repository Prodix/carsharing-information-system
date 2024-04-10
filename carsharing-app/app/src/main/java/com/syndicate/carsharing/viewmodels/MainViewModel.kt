package com.syndicate.carsharing.viewmodels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.syndicate.carsharing.models.MainModel
import com.syndicate.carsharing.data.Tag
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CircleMapObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainModel(null))
    private val _currentLocation = MutableStateFlow(Point())
    private val _page = MutableStateFlow("radarIntro")
    private val _scrimColor = MutableStateFlow(Color.Transparent)
    private val _carType = MutableStateFlow(1f)
    private val _listTags = MutableStateFlow(listOf(
        Tag(
            "child1",
            false
        ),
        Tag(
            "child2",
            false
        ),
        Tag(
            "child3",
            false
        ),
    ))
    private val _circle: MutableStateFlow<CircleMapObject?> = MutableStateFlow(null)
    private val _mem = MutableStateFlow(1f)
    private val _isGesturesEnabled = MutableStateFlow(true)
    private val _walkMinutes = MutableStateFlow(1)

    val uiState = _uiState.asStateFlow()
    val listTags = _listTags.asStateFlow()
    val currentLocation = _currentLocation.asStateFlow()
    val page = _page.asStateFlow()
    val carType = _carType.asStateFlow()
    val mem = _mem.asStateFlow()
    val circle = _circle.asStateFlow()
    val isGesturesEnabled = _isGesturesEnabled.asStateFlow()
    val walkMinutes = _walkMinutes.asStateFlow()
    val scrimColor = _scrimColor.asStateFlow()

    init {
        fillTags()
    }

    fun fillTags() {
        //TODO: Написать заполнение тегов
    }

    fun updateCircle(circle: CircleMapObject?) {
        _circle.update {
            circle
        }
    }

    fun updateScrim(color: Color) {
        _scrimColor.update {
            color
        }
    }

    fun updateCarType(carType: Float) {
        _carType.update {
            carType
        }
    }

    fun updateMem(mem: Float) {
        _mem.update {
            mem
        }
    }

    fun updateIsGesturesEnabled(isGesturesEnabled: Boolean) {
        _isGesturesEnabled.update {
            isGesturesEnabled
        }
    }

    fun updateWalkMinutes(walkMinutes: Int) {
        _walkMinutes.update {
            walkMinutes
        }
    }

    fun updateLocation(location: Point) {
        _currentLocation.update {
            location
        }
    }

    fun updatePage(page: String) {
        _page.update {
            page
        }
    }

    fun updateTags(index: Int) {
        _listTags.update {

            val newList = it.toMutableList()

            newList[index] = Tag(
                newList[index].tag,
                !newList[index].isSelected
            )

            newList
        }
    }
}