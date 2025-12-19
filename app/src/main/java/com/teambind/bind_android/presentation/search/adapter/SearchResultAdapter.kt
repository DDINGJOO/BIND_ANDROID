package com.teambind.bind_android.presentation.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.teambind.bind_android.R
import com.teambind.bind_android.data.model.response.PlaceDto
import com.teambind.bind_android.databinding.ItemSearchResultBinding

class SearchResultAdapter(
    private val onItemClick: (PlaceDto) -> Unit
) : ListAdapter<PlaceDto, SearchResultAdapter.SearchResultViewHolder>(SearchResultDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val binding = ItemSearchResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SearchResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SearchResultViewHolder(
        private val binding: ItemSearchResultBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(place: PlaceDto) {
            with(binding) {
                tvPlaceName.text = place.name
                tvAddress.text = place.address

                if (!place.thumbnailUrl.isNullOrEmpty()) {
                    Glide.with(ivPlace.context)
                        .load(place.thumbnailUrl)
                        .placeholder(R.drawable.bg_rounded_rect)
                        .centerCrop()
                        .into(ivPlace)
                } else {
                    ivPlace.setImageResource(R.drawable.bg_rounded_rect)
                }

                root.setOnClickListener {
                    onItemClick(place)
                }
            }
        }
    }

    class SearchResultDiffCallback : DiffUtil.ItemCallback<PlaceDto>() {
        override fun areItemsTheSame(oldItem: PlaceDto, newItem: PlaceDto): Boolean {
            return oldItem.placeId == newItem.placeId
        }

        override fun areContentsTheSame(oldItem: PlaceDto, newItem: PlaceDto): Boolean {
            return oldItem == newItem
        }
    }
}
