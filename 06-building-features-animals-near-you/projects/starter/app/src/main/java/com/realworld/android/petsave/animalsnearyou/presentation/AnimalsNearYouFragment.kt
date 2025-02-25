/*
 * Copyright (c) 2022 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.realworld.android.petsave.animalsnearyou.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.realworld.android.petsave.R
import com.realworld.android.petsave.common.presentation.AnimalsAdapter
import com.realworld.android.petsave.common.presentation.Event
import com.realworld.android.petsave.databinding.FragmentAnimalsNearYouBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AnimalsNearYouFragment : Fragment() {

  companion object {
    private const val ITEMS_PER_ROW = 2
  }

  private val viewModel: AnimalsNearYouFragmentViewModel by viewModels()
  private val binding get() = _binding!!

  private var _binding: FragmentAnimalsNearYouBinding? = null

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    _binding = FragmentAnimalsNearYouBinding.inflate(inflater, container, false)

    return binding.root
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    setupUI()
    requestInitialAnimalsList()
  }


  private fun setupUI() {
    val adapter = createAdapter() // 3
    setupRecyclerView(adapter)
    subscribeToViewStateUpdates(adapter)
  }

  private fun createAdapter(): AnimalsAdapter {
    return AnimalsAdapter()
  }

  private fun setupRecyclerView(animalsNearYouAdapter: AnimalsAdapter) {
    binding.animalsRecyclerView.apply {
      adapter = animalsNearYouAdapter
      layoutManager = GridLayoutManager(requireContext(), ITEMS_PER_ROW)
      setHasFixedSize(true)
      addOnScrollListener(createInfiniteScrollListener(layoutManager as GridLayoutManager))
    }
  }

  private fun createInfiniteScrollListener(
    layoutManager: GridLayoutManager
  ): RecyclerView.OnScrollListener {
    return object : InfiniteScrollListener(
      layoutManager,
      AnimalsNearYouFragmentViewModel.UI_PAGE_SIZE
    ) {
      override fun loadMoreItems() { requestMoreAnimals() }
      override fun isLoading(): Boolean = viewModel.isLoadingMoreAnimals
      override fun isLastPage(): Boolean = viewModel.isLastPage
    }
  }

  private fun requestMoreAnimals() {
    viewModel.onEvent(AnimalsNearYouEvent.RequestMoreAnimals)
  }

  private fun subscribeToViewStateUpdates(adapter: AnimalsAdapter) {
    viewLifecycleOwner.lifecycleScope.launch { // 1
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) { // 2
        viewModel.state.collect { // 3
          updateScreenState(it, adapter)
        }
      }
    }
  }

  private fun updateScreenState(
    state: AnimalsNearYouViewState,
    adapter: AnimalsAdapter
  ) {
    // 1
    binding.progressBar.isVisible = state.loading
    adapter.submitList(state.animals)
    handleNoMoreAnimalsNearby(state.noMoreAnimalsNearby)
    handleFailures(state.failure)
  }

  // 2
  private fun handleNoMoreAnimalsNearby(noMoreAnimalsNearby: Boolean) {

  }

  // 3
  private fun handleFailures(failure: Event<Throwable>?) {
    val unhandledFailure = failure?.getContentIfNotHandled() ?: return

    val fallbackMessage = getString(R.string.an_error_occurred)
    val snackbarMessage = if (unhandledFailure.message.isNullOrEmpty()) {
      fallbackMessage
    }
    else {
      unhandledFailure.message!! // 4
    }

    if (snackbarMessage.isNotEmpty()) {
      Snackbar.make(requireView(), snackbarMessage, Snackbar.LENGTH_SHORT).show()
    }
  }

  private fun requestInitialAnimalsList() {
    viewModel.onEvent(AnimalsNearYouEvent.RequestInitialAnimalsList)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}
