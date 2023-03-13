package com.realworld.android.petsave.animalsnearyou.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realworld.android.logging.Logger
import com.realworld.android.petsave.animalsnearyou.domain.usecases.GetAnimals
import com.realworld.android.petsave.animalsnearyou.domain.usecases.RequestNextPageOfAnimals
import com.realworld.android.petsave.common.domain.model.NetworkException
import com.realworld.android.petsave.common.domain.model.NetworkUnavailableException
import com.realworld.android.petsave.common.domain.model.NoMoreAnimalsException
import com.realworld.android.petsave.common.domain.model.pagination.Pagination
import com.realworld.android.petsave.common.presentation.Event
import com.realworld.android.petsave.common.presentation.model.UIAnimal
import com.realworld.android.petsave.common.presentation.model.mappers.UiAnimalMapper
import com.realworld.android.petsave.common.utils.createExceptionHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnimalsNearYouFragmentViewModel @Inject constructor(
    private val getAnimals: GetAnimals,
    private val requestNextPageOfAnimals: RequestNextPageOfAnimals,
    private val uiAnimalMapper: UiAnimalMapper,
    private val compositeDisposable: CompositeDisposable
): ViewModel() {

    companion object {
        const val UI_PAGE_SIZE = Pagination.DEFAULT_PAGE_SIZE
    }

    init {
        subscribeToAnimalUpdates()
    }

    private val _state = MutableStateFlow(AnimalsNearYouViewState())
    private var currentPage = 0 // 2

    val state: StateFlow<AnimalsNearYouViewState> = _state.asStateFlow()

    val isLastPage: Boolean
        get() = state.value.noMoreAnimalsNearby

    var isLoadingMoreAnimals: Boolean = false
        private set

    fun onEvent(event: AnimalsNearYouEvent) {
        when(event) {
            is AnimalsNearYouEvent.RequestInitialAnimalsList -> loadAnimals()
            is AnimalsNearYouEvent.RequestMoreAnimals -> loadNextAnimalPage()
        }
    }

    private fun subscribeToAnimalUpdates() {
        getAnimals()
            .map { animals -> animals.map { uiAnimalMapper.mapToView(it) } } // 1
            .subscribeOn(Schedulers.io()) // 2
            .observeOn(AndroidSchedulers.mainThread()) // 3
            .subscribe(
                { onNewAnimalList(it) }, // 4
                { onFailure(it) }
            )
            .addTo(compositeDisposable) // 5
    }

    private fun onNewAnimalList(animals: List<UIAnimal>) {
        Logger.d("Got more animals!")

        // 1
        val updatedAnimalSet = (state.value.animals + animals).toSet()

        // 2
        _state.update { oldState ->
            oldState.copy(
                loading = false,
                animals = updatedAnimalSet.toList()
            )
        }
    }


    private fun loadAnimals() {
        if (state.value.animals.isEmpty()) {
            loadNextAnimalPage()
        }
    }

    private fun loadNextAnimalPage() {
        isLoadingMoreAnimals = true
        val errorMessage = "Failed to fetch nearby animals"
        val exceptionHandler = viewModelScope.createExceptionHandler(errorMessage) { onFailure(it) }

        viewModelScope.launch(exceptionHandler) {
            Logger.d("Requesting more animals.")
            val pagination = requestNextPageOfAnimals(++currentPage)

            onPaginationInfoObtained(pagination)
            isLoadingMoreAnimals = false
        }
    }

    private fun onPaginationInfoObtained(pagination: Pagination) {
        currentPage = pagination.currentPage
    }

    private fun onFailure(failure: Throwable) {
        when (failure) {
            is NetworkException, // 1
            is NetworkUnavailableException -> {
                _state.update { oldState -> // 2
                    oldState.copy(
                        loading = false,
                        failure = Event(failure) // 3
                    )
                }
            }
            is NoMoreAnimalsException -> {
                _state.update { oldState ->
                    oldState.copy(
                        noMoreAnimalsNearby = true,
                        failure = Event(failure)
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear() // 3
    }
}