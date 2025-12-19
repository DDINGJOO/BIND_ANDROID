package com.teambind.bind_android.presentation.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.teambind.bind_android.databinding.ItemRecentSearchBinding

class RecentSearchAdapter(
    private val onItemClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : ListAdapter<String, RecentSearchAdapter.RecentSearchViewHolder>(RecentSearchDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSearchViewHolder {
        val binding = ItemRecentSearchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecentSearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentSearchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecentSearchViewHolder(
        private val binding: ItemRecentSearchBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(query: String) {
            binding.tvSearchQuery.text = query

            binding.root.setOnClickListener {
                onItemClick(query)
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick(query)
            }
        }
    }

    class RecentSearchDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}
