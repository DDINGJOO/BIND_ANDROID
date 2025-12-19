package com.teambind.bind_android.presentation.selecttime.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.teambind.bind_android.R
import com.teambind.bind_android.databinding.ItemTimeSlotBinding
import java.text.NumberFormat
import java.util.*

data class TimeSlotItem(
    val time: String,           // 표시용 시간 (HH:mm)
    val originalTime: String = time,  // API 전송용 원본 시간 (HH:mm:ss)
    val isAvailable: Boolean,
    val isSelected: Boolean = false,
    val price: Int = 0  // 슬롯 가격 (PricingPolicy에서 조회)
)

class TimeSlotAdapter(
    private val onTimeSlotClick: (Int, TimeSlotItem) -> Unit
) : ListAdapter<TimeSlotItem, TimeSlotAdapter.TimeSlotViewHolder>(TimeSlotDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val binding = ItemTimeSlotBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TimeSlotViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class TimeSlotViewHolder(
        private val binding: ItemTimeSlotBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)

        fun bind(item: TimeSlotItem, position: Int) {
            with(binding) {
                tvTime.text = item.time

                // 가격 표시
                tvPrice.text = if (item.price > 0) {
                    "${numberFormat.format(item.price)}원"
                } else {
                    ""
                }

                val backgroundRes = when {
                    item.isSelected -> R.drawable.bg_time_slot_selected
                    item.isAvailable -> R.drawable.bg_time_slot_available
                    else -> R.drawable.bg_time_slot_unavailable
                }
                layoutTimeSlot.setBackgroundResource(backgroundRes)

                val textColor = when {
                    item.isSelected -> R.color.white
                    item.isAvailable -> R.color.black
                    else -> R.color.gray_600
                }
                tvTime.setTextColor(itemView.context.getColor(textColor))

                val priceColor = when {
                    item.isSelected -> R.color.white
                    item.isAvailable -> R.color.gray_600
                    else -> R.color.gray_400
                }
                tvPrice.setTextColor(itemView.context.getColor(priceColor))

                root.setOnClickListener {
                    if (item.isAvailable) {
                        onTimeSlotClick(position, item)
                    }
                }
            }
        }
    }

    class TimeSlotDiffCallback : DiffUtil.ItemCallback<TimeSlotItem>() {
        override fun areItemsTheSame(oldItem: TimeSlotItem, newItem: TimeSlotItem): Boolean {
            return oldItem.time == newItem.time
        }

        override fun areContentsTheSame(oldItem: TimeSlotItem, newItem: TimeSlotItem): Boolean {
            return oldItem == newItem
        }
    }
}
