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

package com.raywenderlich.android.petsave.common.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.android.petsave.common.presentation.model.UIAnimal
import com.raywenderlich.android.petsave.common.utils.setImageWithCrossFade
import com.raywenderlich.android.petsave.databinding.RecyclerViewAnimalItemBinding

class AnimalsAdapter: ListAdapter<UIAnimal, AnimalsAdapter.AnimalsViewHolder>(
    ITEM_COMPARATOR) {

  private var animalClickListener: AnimalClickListener? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimalsViewHolder {
    val binding = RecyclerViewAnimalItemBinding
        .inflate(LayoutInflater.from(parent.context), parent, false)

    return AnimalsViewHolder(binding)
  }

  override fun onBindViewHolder(holder: AnimalsViewHolder, position: Int) {
    val item: UIAnimal = getItem(position)

    holder.bind(item)
  }

  inner class AnimalsViewHolder(
      private val binding: RecyclerViewAnimalItemBinding
  ) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: UIAnimal) {
      binding.name.text = item.name
      binding.photo.setImageWithCrossFade(item.photo)

      binding.root.setOnClickListener {
        animalClickListener?.onClick(item.id)
      }
    }
  }

  fun setOnAnimalClickListener(animalClickListener: AnimalClickListener) {
    this.animalClickListener = animalClickListener
  }
}

private val ITEM_COMPARATOR = object : DiffUtil.ItemCallback<UIAnimal>() {
  override fun areItemsTheSame(oldItem: UIAnimal, newItem: UIAnimal): Boolean {
    return oldItem.id == newItem.id
  }

  override fun areContentsTheSame(oldItem: UIAnimal, newItem: UIAnimal): Boolean {
    return oldItem.name == newItem.name && oldItem.photo == newItem.photo
  }
}

interface AnimalClickListener {
  fun onClick(animalId: Long)
}