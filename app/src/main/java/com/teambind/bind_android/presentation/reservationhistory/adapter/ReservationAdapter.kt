package com.teambind.bind_android.presentation.reservationhistory.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.teambind.bind_android.R
import com.teambind.bind_android.data.model.response.MyReservationDto
import com.teambind.bind_android.databinding.ItemReservationBinding
import java.text.NumberFormat
import java.util.*

enum class ReservationStatus(val value: String, val displayName: String) {
    PENDING("PENDING", "대기중"),
    CONFIRMED("CONFIRMED", "예약 확정"),
    COMPLETED("COMPLETED", "이용 완료"),
    CANCELLED("CANCELLED", "취소됨")
}

class ReservationAdapter(
    private val onItemClick: (MyReservationDto) -> Unit
) : ListAdapter<MyReservationDto, ReservationAdapter.ReservationViewHolder>(ReservationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val binding = ItemReservationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReservationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReservationViewHolder(
        private val binding: ItemReservationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(reservation: MyReservationDto) {
            with(binding) {
                // Status
                val status = ReservationStatus.entries.find { it.value == reservation.status }
                    ?: ReservationStatus.PENDING
                tvStatus.text = status.displayName

                // Status badge color
                val statusColor = when (status) {
                    ReservationStatus.CONFIRMED -> R.color.primary_yellow
                    ReservationStatus.COMPLETED -> R.color.gray_400
                    ReservationStatus.CANCELLED -> R.color.error_red
                    ReservationStatus.PENDING -> R.color.gray_300
                }
                tvStatus.backgroundTintList = itemView.context.getColorStateList(statusColor)

                // Reservation date
                tvReservationDate.text = reservation.reservationDate.replace("-", ".")

                // Studio image
                if (!reservation.firstImageUrl.isNullOrEmpty()) {
                    Glide.with(ivStudio.context)
                        .load(reservation.firstImageUrl)
                        .placeholder(R.drawable.bg_rounded_rect)
                        .error(R.drawable.bg_rounded_rect)
                        .centerCrop()
                        .into(ivStudio)
                } else {
                    ivStudio.setImageResource(R.drawable.bg_rounded_rect)
                }

                // Studio info
                tvStudioName.text = reservation.placeName ?: "장소"
                tvRoomName.text = reservation.roomName ?: "룸"

                // DateTime - 시작 시간과 종료 시간 계산 (30분 슬롯 기준)
                val date = reservation.reservationDate.replace("-", ".")
                val timeRange = formatTimeRange(reservation.startTimes)
                tvDateTime.text = if (timeRange.isNotEmpty()) "$date $timeRange" else date

                // Price
                val formatter = NumberFormat.getNumberInstance(Locale.KOREA)
                tvPrice.text = "${formatter.format(reservation.totalPrice)}원"

                // Click listener
                root.setOnClickListener {
                    onItemClick(reservation)
                }
            }
        }

        /**
         * 시작 시간 리스트에서 시작~종료 시간 포맷
         * 예: ["09:00"] -> "09:00 ~ 10:00 (1시간)"
         * 예: ["09:00", "10:00"] -> "09:00 ~ 11:00 (2시간)"
         */
        private fun formatTimeRange(startTimes: List<String>): String {
            if (startTimes.isEmpty()) return ""

            val startTime = startTimes.first()
            val lastSlotStart = startTimes.last()
            // 각 슬롯은 1시간 단위이므로 마지막 슬롯에 1시간 추가
            val endTime = addOneHour(lastSlotStart)

            // 총 시간 계산 (슬롯 개수 * 1시간)
            val totalHours = startTimes.size

            return "$startTime ~ $endTime (${totalHours}시간)"
        }

        /**
         * 시작 시간에 1시간을 더해서 종료 시간 계산
         */
        private fun addOneHour(startTime: String): String {
            return try {
                val parts = startTime.split(":")
                val hour = parts[0].toInt()
                val minute = parts[1].toInt()
                var endHour = hour + 1
                if (endHour >= 24) endHour = 0
                String.format("%02d:%02d", endHour, minute)
            } catch (e: Exception) {
                startTime
            }
        }
    }

    class ReservationDiffCallback : DiffUtil.ItemCallback<MyReservationDto>() {
        override fun areItemsTheSame(oldItem: MyReservationDto, newItem: MyReservationDto): Boolean {
            return oldItem.reservationId == newItem.reservationId
        }

        override fun areContentsTheSame(oldItem: MyReservationDto, newItem: MyReservationDto): Boolean {
            return oldItem == newItem
        }
    }
}
