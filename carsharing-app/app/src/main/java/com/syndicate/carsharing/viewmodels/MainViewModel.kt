package com.syndicate.carsharing.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import com.syndicate.carsharing.models.MainModel
import com.syndicate.carsharing.data.Tag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainModel(null))
    val uiState = _uiState.asStateFlow()

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

    val listTags = _listTags.asStateFlow()

    init {
        fillTags()
    }

    fun fillTags() {
        //TODO: Написать заполнение тегов
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