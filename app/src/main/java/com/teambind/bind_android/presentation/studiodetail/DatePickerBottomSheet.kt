package com.teambind.bind_android.presentation.studiodetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.teambind.bind_android.R
import com.teambind.bind_android.databinding.BottomSheetDatePickerBinding
import com.teambind.bind_android.databinding.ItemCalendarDayBinding
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class DatePickerBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetDatePickerBinding? = null
    private val binding get() = _binding!!

    private var currentYearMonth: YearMonth = YearMonth.now()
    private var selectedDate: LocalDate? = null
    private var onDateSelected: ((LocalDate) -> Unit)? = null

    private val calendarAdapter by lazy { CalendarAdapter() }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetDatePickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCalendar()
        setupClickListeners()
        updateCalendar()
    }

    private fun setupCalendar() {
        binding.rvCalendar.apply {
            adapter = calendarAdapter
            layoutManager = GridLayoutManager(requireContext(), 7)
            itemAnimator = null
        }
    }

    private fun setupClickListeners() {
        binding.btnPrevMonth.setOnClickListener {
            currentYearMonth = currentYearMonth.minusMonths(1)
            updateCalendar()
        }

        binding.btnNextMonth.setOnClickListener {
            currentYearMonth = currentYearMonth.plusMonths(1)
            updateCalendar()
        }

        binding.btnConfirm.setOnClickListener {
            selectedDate?.let { date ->
                onDateSelected?.invoke(date)
                dismiss()
            }
        }
    }

    private fun updateCalendar() {
        val formatter = DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREAN)
        binding.tvYearMonth.text = currentYearMonth.format(formatter)

        val days = generateCalendarDays(currentYearMonth)
        calendarAdapter.submitList(days)
    }

    private fun generateCalendarDays(yearMonth: YearMonth): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        val firstDayOfMonth = yearMonth.atDay(1)
        val lastDayOfMonth = yearMonth.atEndOfMonth()
        val today = LocalDate.now()

        // 첫 주 시작 전 빈 칸 채우기 (일요일 = 0)
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
        repeat(firstDayOfWeek) {
            days.add(CalendarDay.Empty)
        }

        // 해당 월의 날짜들
        for (day in 1..lastDayOfMonth.dayOfMonth) {
            val date = yearMonth.atDay(day)
            val isPast = date.isBefore(today)
            val isToday = date == today
            val isSelected = date == selectedDate

            days.add(
                CalendarDay.Day(
                    date = date,
                    dayOfMonth = day,
                    isToday = isToday,
                    isPast = isPast,
                    isSelected = isSelected,
                    isSunday = date.dayOfWeek.value == 7,
                    isSaturday = date.dayOfWeek.value == 6
                )
            )
        }

        return days
    }

    fun setOnDateSelectedListener(listener: (LocalDate) -> Unit) {
        onDateSelected = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Calendar Adapter
    private inner class CalendarAdapter : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

        private var days: List<CalendarDay> = emptyList()

        fun submitList(newDays: List<CalendarDay>) {
            days = newDays
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
            val binding = ItemCalendarDayBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return DayViewHolder(binding)
        }

        override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
            holder.bind(days[position])
        }

        override fun getItemCount(): Int = days.size

        inner class DayViewHolder(
            private val itemBinding: ItemCalendarDayBinding
        ) : RecyclerView.ViewHolder(itemBinding.root) {

            fun bind(calendarDay: CalendarDay) {
                when (calendarDay) {
                    is CalendarDay.Empty -> {
                        itemBinding.tvDay.text = ""
                        itemBinding.tvDay.background = null
                        itemBinding.root.isClickable = false
                    }
                    is CalendarDay.Day -> {
                        itemBinding.tvDay.text = calendarDay.dayOfMonth.toString()

                        // 배경 설정
                        when {
                            calendarDay.isSelected -> {
                                itemBinding.tvDay.setBackgroundResource(R.drawable.bg_calendar_selected)
                            }
                            calendarDay.isToday -> {
                                itemBinding.tvDay.setBackgroundResource(R.drawable.bg_calendar_today)
                            }
                            else -> {
                                itemBinding.tvDay.background = null
                            }
                        }

                        // 텍스트 색상
                        val textColor = when {
                            calendarDay.isPast -> R.color.gray3
                            calendarDay.isSelected -> R.color.black
                            calendarDay.isSunday -> R.color.error_red
                            calendarDay.isSaturday -> R.color.custom_blue
                            else -> R.color.black
                        }
                        itemBinding.tvDay.setTextColor(
                            ContextCompat.getColor(itemBinding.root.context, textColor)
                        )

                        // 클릭 리스너
                        if (!calendarDay.isPast) {
                            itemBinding.root.setOnClickListener {
                                selectedDate = calendarDay.date
                                binding.btnConfirm.isEnabled = true
                                updateCalendar()
                            }
                        } else {
                            itemBinding.root.setOnClickListener(null)
                        }
                        itemBinding.root.isClickable = !calendarDay.isPast
                    }
                }
            }
        }
    }

    // Calendar Day Model
    sealed class CalendarDay {
        object Empty : CalendarDay()
        data class Day(
            val date: LocalDate,
            val dayOfMonth: Int,
            val isToday: Boolean,
            val isPast: Boolean,
            val isSelected: Boolean,
            val isSunday: Boolean,
            val isSaturday: Boolean
        ) : CalendarDay()
    }

    companion object {
        fun newInstance(): DatePickerBottomSheet {
            return DatePickerBottomSheet()
        }
    }
}
