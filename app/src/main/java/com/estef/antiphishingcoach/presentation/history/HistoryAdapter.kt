package com.estef.antiphishingcoach.presentation.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.estef.antiphishingcoach.databinding.ItemHistoryIncidentBinding
import com.estef.antiphishingcoach.presentation.common.renderRiskGauge

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
            riskGaugeHistory.renderRiskGauge(item.score)
            chipSignal1.isVisible = item.signalTags.size >= 1
            chipSignal2.isVisible = item.signalTags.size >= 2
            if (item.signalTags.size >= 1) {
                chipSignal1.text = item.signalTags[0]
            }
            if (item.signalTags.size >= 2) {
                chipSignal2.text = item.signalTags[1]
            }
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
