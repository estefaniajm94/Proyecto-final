package com.estef.antiphishingcoach.presentation.resources

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.databinding.ItemOfficialResourceBinding

class ResourcesAdapter(
    private val onOpenUrl: (String) -> Unit,
    private val onDialPhone: (String) -> Unit
) : ListAdapter<OfficialResourceItem, ResourcesAdapter.ResourceViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResourceViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemOfficialResourceBinding.inflate(inflater, parent, false)
        return ResourceViewHolder(binding, onOpenUrl, onDialPhone)
    }

    override fun onBindViewHolder(holder: ResourceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ResourceViewHolder(
        private val binding: ItemOfficialResourceBinding,
        private val onOpenUrl: (String) -> Unit,
        private val onDialPhone: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OfficialResourceItem) = with(binding) {
            tvResourceTitle.text = item.title
            tvResourceDescription.text = item.description

            val phone = item.phone
            if (phone.isNullOrBlank()) {
                btnDial.visibility = android.view.View.GONE
            } else {
                btnDial.visibility = android.view.View.VISIBLE
                btnDial.text = root.context.getString(R.string.resources_call_button, phone)
                btnDial.setOnClickListener { onDialPhone(phone) }
            }

            val url = item.url
            if (url.isNullOrBlank()) {
                btnOpenUrl.visibility = android.view.View.GONE
            } else {
                btnOpenUrl.visibility = android.view.View.VISIBLE
                btnOpenUrl.setOnClickListener { onOpenUrl(url) }
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<OfficialResourceItem>() {
        override fun areItemsTheSame(oldItem: OfficialResourceItem, newItem: OfficialResourceItem): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: OfficialResourceItem, newItem: OfficialResourceItem): Boolean {
            return oldItem == newItem
        }
    }
}
