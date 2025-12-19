package com.teambind.bind_android.presentation.communitydetail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.teambind.bind_android.R
import com.teambind.bind_android.data.model.response.ImageDto
import com.teambind.bind_android.databinding.ItemArticleImageBinding

class ArticleImageAdapter(
    private val onImageClick: (ImageDto, Int) -> Unit
) : ListAdapter<ImageDto, ArticleImageAdapter.ImageViewHolder>(ImageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemArticleImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class ImageViewHolder(
        private val binding: ItemArticleImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(image: ImageDto, position: Int) {
            Glide.with(binding.ivImage)
                .load(image.imageUrl)
                .placeholder(R.drawable.bg_rounded_rect)
                .centerCrop()
                .into(binding.ivImage)

            binding.root.setOnClickListener {
                onImageClick(image, position)
            }
        }
    }

    class ImageDiffCallback : DiffUtil.ItemCallback<ImageDto>() {
        override fun areItemsTheSame(oldItem: ImageDto, newItem: ImageDto): Boolean {
            return oldItem.imageId == newItem.imageId
        }

        override fun areContentsTheSame(oldItem: ImageDto, newItem: ImageDto): Boolean {
            return oldItem == newItem
        }
    }
}
