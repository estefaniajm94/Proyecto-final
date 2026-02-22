package com.estef.antiphishingcoach.presentation.coach

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.estef.antiphishingcoach.databinding.ItemCoachScenarioBinding
import com.estef.antiphishingcoach.domain.model.CoachScenario

class CoachScenarioAdapter(
    private val onOpenChecklist: (CoachScenario) -> Unit
) : ListAdapter<CoachScenario, CoachScenarioAdapter.CoachScenarioViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoachScenarioViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCoachScenarioBinding.inflate(inflater, parent, false)
        return CoachScenarioViewHolder(binding, onOpenChecklist)
    }

    override fun onBindViewHolder(holder: CoachScenarioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CoachScenarioViewHolder(
        private val binding: ItemCoachScenarioBinding,
        private val onOpenChecklist: (CoachScenario) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CoachScenario) = with(binding) {
            tvScenarioTitle.text = item.title
            btnOpenChecklist.setOnClickListener {
                onOpenChecklist(item)
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<CoachScenario>() {
        override fun areItemsTheSame(oldItem: CoachScenario, newItem: CoachScenario): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CoachScenario, newItem: CoachScenario): Boolean {
            return oldItem == newItem
        }
    }
}
