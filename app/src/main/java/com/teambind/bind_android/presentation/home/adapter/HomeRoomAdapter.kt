package com.teambind.bind_android.presentation.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.teambind.bind_android.R
import com.teambind.bind_android.data.model.response.RoomDto
import com.teambind.bind_android.databinding.ItemStudioBinding

class HomeRoomAdapter(
    private val onItemClick: (RoomDto) -> Unit
) : ListAdapter<RoomDto, HomeRoomAdapter.RoomViewHolder>(RoomDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val binding = ItemStudioBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RoomViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RoomViewHolder(
        private val binding: ItemStudioBinding,
        private val onItemClick: (RoomDto) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(room: RoomDto) {
            with(binding) {
                // 연습실 이름
                tvName.text = room.name

                // 주소 대신 시간대 정보
                tvAddress.text = room.timeSlot ?: ""

                // 평점 - 연습실에는 평점이 없으므로 숨김
                tvRating.text = "-"
                tvReviewCount.text = ""

                // 최대 인원
                val maxOccupancy = room.maxOccupancy ?: 0
                tvPrice.text = if (maxOccupancy > 0) "최대 ${maxOccupancy}명" else ""

                // 키워드 - 연습실에는 키워드가 없음
                tvKeywords.text = ""

                // 썸네일 이미지
                val imageUrl = room.images?.firstOrNull()?.imageUrl
                    ?: room.imageUrls?.firstOrNull()
                Glide.with(ivThumbnail)
                    .load(imageUrl)
                    .placeholder(R.drawable.bg_rounded_rect)
                    .centerCrop()
                    .into(ivThumbnail)

                // 클릭 리스너
                root.setOnClickListener {
                    onItemClick(room)
                }
            }
        }
    }

    class RoomDiffCallback : DiffUtil.ItemCallback<RoomDto>() {
        override fun areItemsTheSame(oldItem: RoomDto, newItem: RoomDto): Boolean {
            return oldItem.roomId == newItem.roomId
        }

        override fun areContentsTheSame(oldItem: RoomDto, newItem: RoomDto): Boolean {
            return oldItem == newItem
        }
    }
}
