package com.estef.antiphishingcoach.presentation.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * Adaptador base para listas simples con ViewBinding y DiffUtil.
 */
abstract class BaseListAdapter<T : Any, VB : ViewBinding>(
    diffCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, BaseListAdapter.BindingViewHolder<VB>>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<VB> {
        val binding = createBinding(LayoutInflater.from(parent.context), parent)
        return BindingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BindingViewHolder<VB>, position: Int) {
        bind(holder.binding, getItem(position))
    }

    protected abstract fun createBinding(inflater: LayoutInflater, parent: ViewGroup): VB
    protected abstract fun bind(binding: VB, item: T)

    class BindingViewHolder<VB : ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root)
}
