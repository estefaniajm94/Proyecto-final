package com.estef.antiphishingcoach.presentation.coach

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.estef.antiphishingcoach.R
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
            tvScenarioThreat.text = item.threatLabel
            tvScenarioThreat.visibility = if (item.threatLabel.isBlank()) {
                android.view.View.GONE
            } else {
                android.view.View.VISIBLE
            }
            tvScenarioTitle.text = item.title
            tvScenarioSummary.text = item.summary
            tvScenarioMeta.text = root.context.getString(
                R.string.coach_list_meta,
                item.threatLabel.ifBlank { item.title },
                item.typicalSigns.size,
                item.whatToDoNow.size
            )
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
