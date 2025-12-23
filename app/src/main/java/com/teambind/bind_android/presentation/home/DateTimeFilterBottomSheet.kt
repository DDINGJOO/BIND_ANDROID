package com.teambind.bind_android.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.teambind.bind_android.R
import com.teambind.bind_android.databinding.BottomSheetDatetimeFilterBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DateTimeSelection(
    val date: String?,       // yyyy-MM-dd 형식
    val startTime: String?,  // HH:mm 형식
    val endTime: String?     // HH:mm 형식
)

class DateTimeFilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetDatetimeFilterBinding? = null
    private val binding get() = _binding!!

    private var selectedDate: String? = null
    private var selectedStartTime: Int = 0
    private var selectedEndTime: Int = 24
    private var onDateTimeSelected: ((DateTimeSelection) -> Unit)? = null

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
    private val displayDateFormat = SimpleDateFormat("M월 d일", Locale.KOREA)

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetDatetimeFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCalendar()
        setupTimeSliders()
        setupButtons()
        updateTimeRangeText()
    }

    private fun setupCalendar() {
        val calendar = Calendar.getInstance()

        // 최소 날짜는 오늘
        binding.calendarView.minDate = calendar.timeInMillis

        // 최대 날짜는 3개월 후
        calendar.add(Calendar.MONTH, 3)
        binding.calendarView.maxDate = calendar.timeInMillis

        // 현재 선택된 날짜가 있으면 설정
        selectedDate?.let { date ->
            try {
                val parsedDate = dateFormat.parse(date)
                parsedDate?.let {
                    binding.calendarView.date = it.time
                }
            } catch (e: Exception) {
                // 파싱 실패 시 무시
            }
        }

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            selectedDate = dateFormat.format(cal.time)
            updateTimeRangeText()
        }
    }

    private fun setupTimeSliders() {
        binding.sliderStartTime.value = selectedStartTime.toFloat()
        binding.sliderEndTime.value = selectedEndTime.toFloat()

        updateStartTimeText()
        updateEndTimeText()

        binding.sliderStartTime.addOnChangeListener { _, value, _ ->
            selectedStartTime = value.toInt()
            // 시작 시간이 종료 시간보다 크면 종료 시간 조정
            if (selectedStartTime >= selectedEndTime) {
                selectedEndTime = minOf(selectedStartTime + 1, 24)
                binding.sliderEndTime.value = selectedEndTime.toFloat()
            }
            updateStartTimeText()
            updateTimeRangeText()
        }

        binding.sliderEndTime.addOnChangeListener { _, value, _ ->
            selectedEndTime = value.toInt()
            // 종료 시간이 시작 시간보다 작으면 시작 시간 조정
            if (selectedEndTime <= selectedStartTime) {
                selectedStartTime = maxOf(selectedEndTime - 1, 0)
                binding.sliderStartTime.value = selectedStartTime.toFloat()
            }
            updateEndTimeText()
            updateTimeRangeText()
        }
    }

    private fun updateStartTimeText() {
        binding.tvStartTime.text = "${selectedStartTime}시"
    }

    private fun updateEndTimeText() {
        binding.tvEndTime.text = "${selectedEndTime}시"
    }

    private fun updateTimeRangeText() {
        val timeText = if (selectedStartTime == 0 && selectedEndTime == 24) {
            "시간 무관"
        } else {
            "${selectedStartTime}시 ~ ${selectedEndTime}시"
        }

        val dateText = selectedDate?.let { date ->
            try {
                val parsedDate = dateFormat.parse(date)
                parsedDate?.let { displayDateFormat.format(it) }
            } catch (e: Exception) {
                null
            }
        }

        binding.tvTimeRange.text = if (dateText != null) {
            "$dateText / $timeText"
        } else {
            timeText
        }
    }

    private fun setupButtons() {
        // 초기화 버튼
        binding.btnReset.setOnClickListener {
            selectedDate = null
            selectedStartTime = 0
            selectedEndTime = 24

            binding.calendarView.date = System.currentTimeMillis()
            binding.sliderStartTime.value = 0f
            binding.sliderEndTime.value = 24f

            updateStartTimeText()
            updateEndTimeText()
            updateTimeRangeText()
        }

        // 적용 버튼
        binding.btnApply.setOnClickListener {
            val startTimeStr = if (selectedStartTime == 0 && selectedEndTime == 24) {
                null
            } else {
                String.format("%02d:00", selectedStartTime)
            }

            val endTimeStr = if (selectedStartTime == 0 && selectedEndTime == 24) {
                null
            } else {
                String.format("%02d:00", selectedEndTime)
            }

            onDateTimeSelected?.invoke(
                DateTimeSelection(
                    date = selectedDate,
                    startTime = startTimeStr,
                    endTime = endTimeStr
                )
            )
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(
            currentDate: String?,
            currentStartTime: String?,
            currentEndTime: String?,
            onDateTimeSelected: (DateTimeSelection) -> Unit
        ): DateTimeFilterBottomSheet {
            return DateTimeFilterBottomSheet().apply {
                this.selectedDate = currentDate
                this.selectedStartTime = currentStartTime?.substringBefore(":")?.toIntOrNull() ?: 0
                this.selectedEndTime = currentEndTime?.substringBefore(":")?.toIntOrNull() ?: 24
                this.onDateTimeSelected = onDateTimeSelected
            }
        }
    }
}
