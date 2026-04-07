package com.estef.antiphishingcoach.presentation.avatar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.core.avatar.AvatarOption
import com.estef.antiphishingcoach.databinding.ItemAvatarOptionBinding
import com.estef.antiphishingcoach.presentation.common.renderAvatar

class AvatarOptionAdapter(
    private val onAvatarSelected: (String) -> Unit
) : RecyclerView.Adapter<AvatarOptionAdapter.AvatarOptionViewHolder>() {

    private var options: List<AvatarOption> = emptyList()
    private var selectedAvatarId: String? = null

    fun submitOptions(items: List<AvatarOption>, selectedId: String) {
        val oldItems = options
        options = items
        selectedAvatarId = selectedId
        DiffUtil.calculateDiff(
            object : DiffUtil.Callback() {
                override fun getOldListSize(): Int = oldItems.size

                override fun getNewListSize(): Int = items.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return oldItems[oldItemPosition].id == items[newItemPosition].id
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return oldItems[oldItemPosition] == items[newItemPosition]
                }
            }
        ).dispatchUpdatesTo(this)
    }

    fun updateSelectedAvatar(avatarId: String) {
        if (selectedAvatarId == avatarId) return
        val previousIndex = options.indexOfFirst { it.id == selectedAvatarId }
        val newIndex = options.indexOfFirst { it.id == avatarId }
        selectedAvatarId = avatarId
        if (previousIndex != -1) notifyItemChanged(previousIndex)
        if (newIndex != -1) notifyItemChanged(newIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatarOptionViewHolder {
        val binding = ItemAvatarOptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AvatarOptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AvatarOptionViewHolder, position: Int) {
        holder.bind(options[position], options[position].id == selectedAvatarId)
    }

    override fun getItemCount(): Int = options.size

    inner class AvatarOptionViewHolder(
        private val binding: ItemAvatarOptionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AvatarOption, isSelected: Boolean) = with(binding) {
            ivAvatarOption.renderAvatar(item.id)

            val context = root.context
            root.strokeWidth = if (isSelected) root.resources.displayMetrics.density.times(2).toInt() else 0
            root.strokeColor = ContextCompat.getColor(
                context,
                if (isSelected) R.color.brand_primary_dark else R.color.card_outline_soft
            )
            root.alpha = if (isSelected) 1f else 0.92f

            root.setOnClickListener {
                if (selectedAvatarId != item.id) {
                    updateSelectedAvatar(item.id)
                }
                onAvatarSelected(item.id)
            }
        }
    }
}
