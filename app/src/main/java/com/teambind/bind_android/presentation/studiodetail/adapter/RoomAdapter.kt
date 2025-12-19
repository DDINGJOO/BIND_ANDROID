package com.teambind.bind_android.presentation.studiodetail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.teambind.bind_android.R
import com.teambind.bind_android.data.model.response.RoomDetailResponse
import com.teambind.bind_android.databinding.ItemRoomBinding
import java.text.NumberFormat
import java.util.Locale

class RoomAdapter(
    private val onItemClick: (RoomDetailResponse) -> Unit
) : ListAdapter<RoomDetailResponse, RoomAdapter.RoomViewHolder>(RoomDiffCallback()) {

    private val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val binding = ItemRoomBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RoomViewHolder(
        private val binding: ItemRoomBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(roomDetail: RoomDetailResponse) {
            val room = roomDetail.room ?: return
            val pricingPolicy = roomDetail.pricingPolicy

            with(binding) {
                tvName.text = room.roomName

                // 시간 단위 표시
                val timeSlotText = when (room.timeSlot) {
                    "HOUR" -> "1시간 단위"
                    "HALF_HOUR" -> "30분 단위"
                    "QUARTER_HOUR" -> "15분 단위"
                    else -> ""
                }
                tvDescription.text = timeSlotText

                // 수용 인원
                val maxOccupancy = room.maxOccupancy
                tvCapacity.text = if (maxOccupancy != null && maxOccupancy > 0) {
                    "최대 ${maxOccupancy}명"
                } else {
                    "10+"
                }

                // 시간당 가격 (pricingPolicy에서)
                if (pricingPolicy != null) {
                    val defaultPrice = pricingPolicy.defaultPrice ?: 0
                    val timeSlot = pricingPolicy.timeSlot ?: "HOUR"

                    // 시간당 가격으로 환산
                    val hourlyPrice = when (timeSlot) {
                        "HALF_HOUR" -> defaultPrice * 2
                        "QUARTER_HOUR" -> defaultPrice * 4
                        else -> defaultPrice
                    }

                    tvPrice.text = "${numberFormat.format(hourlyPrice)}원/시간"
                    tvPrice.visibility = View.VISIBLE
                } else {
                    tvPrice.visibility = View.GONE
                }

                // 썸네일 이미지 로드
                val imageUrl = room.imageUrls?.firstOrNull() ?: room.images?.firstOrNull()?.imageUrl
                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(ivThumbnail.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.bg_rounded_rect)
                        .centerCrop()
                        .into(ivThumbnail)
                } else {
                    ivThumbnail.setImageResource(R.drawable.bg_rounded_rect)
                }
            }
        }
    }

    class RoomDiffCallback : DiffUtil.ItemCallback<RoomDetailResponse>() {
        override fun areItemsTheSame(oldItem: RoomDetailResponse, newItem: RoomDetailResponse): Boolean {
            return oldItem.room?.roomId == newItem.room?.roomId
        }

        override fun areContentsTheSame(oldItem: RoomDetailResponse, newItem: RoomDetailResponse): Boolean {
            return oldItem == newItem
        }
    }
}
