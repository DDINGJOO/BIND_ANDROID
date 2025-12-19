package com.teambind.bind_android.presentation.studiodetail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.teambind.bind_android.R
import com.teambind.bind_android.databinding.ItemImageBinding

class ImagePagerAdapter(
    private val images: List<String>,
    private val onImageClick: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images[position], position)
    }

    override fun getItemCount(): Int = images.size

    inner class ImageViewHolder(
        private val binding: ItemImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(imageUrl: String, position: Int) {
            Glide.with(binding.ivImage.context)
                .load(imageUrl)
                .placeholder(R.drawable.bg_rounded_rect)
                .centerCrop()
                .into(binding.ivImage)

            binding.root.setOnClickListener {
                onImageClick?.invoke(position)
            }
        }
    }
}
