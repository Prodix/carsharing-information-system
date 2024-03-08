package com.syndicate.carsharing.models

import androidx.compose.runtime.MutableState


data class MainModel (
    val tags: List<Tag>?
)

data class Tag (
    var tag: String,
    var isSelected: MutableState<Boolean>
)