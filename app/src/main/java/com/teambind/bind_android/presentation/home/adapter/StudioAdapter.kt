package com.teambind.bind_android.presentation.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.teambind.bind_android.R
import com.teambind.bind_android.data.model.response.StudioDto
import com.teambind.bind_android.databinding.ItemStudioBinding
import java.text.NumberFormat
import java.util.*

class StudioAdapter(
    private val onItemClick: (StudioDto) -> Unit
) : ListAdapter<StudioDto, StudioAdapter.StudioViewHolder>(StudioDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudioViewHolder {
        val binding = ItemStudioBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StudioViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: StudioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class StudioViewHolder(
        private val binding: ItemStudioBinding,
        private val onItemClick: (StudioDto) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)

        fun bind(studio: StudioDto) {
            with(binding) {
                // 스튜디오 이름
                tvName.text = studio.name ?: ""

                // 주소
                tvAddress.text = studio.address ?: ""

                // 평점
                tvRating.text = studio.rating?.let { String.format("%.1f", it) } ?: "-"

                // 리뷰 수
                tvReviewCount.text = "(${studio.reviewCount ?: 0})"

                // 연습실 개수 표시 (가격 대신)
                val roomCount = studio.roomCount ?: 0
                tvPrice.text = if (roomCount > 0) "연습실 ${roomCount}개" else ""

                // 키워드
                val keywords = studio.keywords ?: emptyList()
                if (keywords.isNotEmpty()) {
                    tvKeywords.text = keywords.take(3).joinToString(" ") { "#$it" }
                } else {
                    tvKeywords.text = ""
                }

                // 썸네일 이미지
                Glide.with(ivThumbnail)
                    .load(studio.thumbnailUrl)
                    .placeholder(R.drawable.bg_rounded_rect)
                    .centerCrop()
                    .into(ivThumbnail)

                // 클릭 리스너
                root.setOnClickListener {
                    onItemClick(studio)
                }
            }
        }
    }

    class StudioDiffCallback : DiffUtil.ItemCallback<StudioDto>() {
        override fun areItemsTheSame(oldItem: StudioDto, newItem: StudioDto): Boolean {
            return oldItem.studioId == newItem.studioId && oldItem.studioId != null
        }

        override fun areContentsTheSame(oldItem: StudioDto, newItem: StudioDto): Boolean {
            return oldItem == newItem
        }
    }
}
