package com.teambind.bind_android.presentation.studiodetail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.teambind.bind_android.R
import java.time.LocalDate

data class CalendarDay(
    val date: LocalDate?,
    val isCurrentMonth: Boolean = true,
    val isToday: Boolean = false,
    val isSelected: Boolean = false,
    val isPastDate: Boolean = false
)

class CalendarDayAdapter(
    private val onDayClick: (LocalDate) -> Unit
) : ListAdapter<CalendarDay, CalendarDayAdapter.CalendarDayViewHolder>(CalendarDayDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarDayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return CalendarDayViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarDayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CalendarDayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDay: TextView = itemView.findViewById(R.id.tvDay)

        fun bind(day: CalendarDay) {
            if (day.date == null) {
                tvDay.text = ""
                tvDay.background = null
                tvDay.isClickable = false
                return
            }

            tvDay.text = day.date.dayOfMonth.toString()

            // 배경 및 텍스트 색상 설정
            when {
                day.isSelected -> {
                    tvDay.setBackgroundResource(R.drawable.bg_calendar_selected)
                    tvDay.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                }
                day.isToday -> {
                    tvDay.setBackgroundResource(R.drawable.bg_calendar_today)
                    tvDay.setTextColor(ContextCompat.getColor(itemView.context, R.color.main_yellow))
                }
                day.isPastDate -> {
                    tvDay.background = null
                    tvDay.setTextColor(ContextCompat.getColor(itemView.context, R.color.gray3))
                }
                !day.isCurrentMonth -> {
                    tvDay.background = null
                    tvDay.setTextColor(ContextCompat.getColor(itemView.context, R.color.gray3))
                }
                else -> {
                    tvDay.background = null
                    // 일요일(0), 토요일(6)
                    val dayOfWeek = day.date.dayOfWeek.value % 7 // Sunday = 0
                    val textColor = when (dayOfWeek) {
                        0 -> R.color.error_red // Sunday
                        6 -> R.color.custom_blue // Saturday
                        else -> R.color.black
                    }
                    tvDay.setTextColor(ContextCompat.getColor(itemView.context, textColor))
                }
            }

            // 클릭 처리
            if (!day.isPastDate && day.isCurrentMonth && day.date != null) {
                tvDay.setOnClickListener {
                    onDayClick(day.date)
                }
            } else {
                tvDay.setOnClickListener(null)
                tvDay.isClickable = false
            }
        }
    }

    class CalendarDayDiffCallback : DiffUtil.ItemCallback<CalendarDay>() {
        override fun areItemsTheSame(oldItem: CalendarDay, newItem: CalendarDay): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: CalendarDay, newItem: CalendarDay): Boolean {
            return oldItem == newItem
        }
    }
}
