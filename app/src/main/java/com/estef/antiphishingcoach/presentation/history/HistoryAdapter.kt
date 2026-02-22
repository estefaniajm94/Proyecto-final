package com.estef.antiphishingcoach.presentation.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.estef.antiphishingcoach.databinding.ItemHistoryIncidentBinding

class HistoryAdapter(
    private val onClick: (Long) -> Unit
) : ListAdapter<HistoryItemUi, HistoryAdapter.HistoryViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemHistoryIncidentBinding.inflate(inflater, parent, false)
        return HistoryViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HistoryViewHolder(
        private val binding: ItemHistoryIncidentBinding,
        private val onClick: (Long) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HistoryItemUi) = with(binding) {
            tvTitle.text = item.title
            tvMeta.text = item.metaLine
            tvCreatedAt.text = item.createdAtLine
            root.setOnClickListener {
                onClick(item.incidentId)
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<HistoryItemUi>() {
        override fun areItemsTheSame(oldItem: HistoryItemUi, newItem: HistoryItemUi): Boolean {
            return oldItem.incidentId == newItem.incidentId
        }

        override fun areContentsTheSame(oldItem: HistoryItemUi, newItem: HistoryItemUi): Boolean {
            return oldItem == newItem
        }
    }
}
