package com.realworld.android.petsave.animalsnearyou.presentation

import com.realworld.android.petsave.common.presentation.Event
import com.realworld.android.petsave.common.presentation.model.UIAnimal

data class AnimalsNearYouViewState(
    val loading: Boolean = true, // 1
    val animals: List<UIAnimal> = emptyList(), // 2
    val noMoreAnimalsNearby: Boolean = false, // 3
    val failure: Event<Throwable>? = null // 4
)