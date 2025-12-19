package com.teambind.bind_android.presentation.studiodetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.teambind.bind_android.R
import com.teambind.bind_android.data.model.response.PricingPolicyDto
import com.teambind.bind_android.databinding.BottomSheetPriceDetailBinding
import java.text.NumberFormat
import java.util.Locale

class PriceDetailBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetPriceDetailBinding? = null
    private val binding get() = _binding!!

    private var pricingPolicy: PricingPolicyDto? = null
    private val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetPriceDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        bindData()
    }

    private fun setupViews() {
        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnConfirm.setOnClickListener { dismiss() }
    }

    private fun bindData() {
        val policy = pricingPolicy ?: return

        val defaultPrice = policy.defaultPrice ?: 0
        val timeSlot = policy.timeSlot ?: "HOUR"

        // 시간 단위 설명
        val unitText = when (timeSlot) {
            "HOUR" -> "1시간"
            "HALF_HOUR" -> "30분"
            "QUARTER_HOUR" -> "15분"
            else -> "1시간"
        }

        binding.tvDefaultPrice.text = "${numberFormat.format(defaultPrice)}원 / $unitText"

        // 시간대별 요금
        val timeRangePrices = policy.timeRangePrices
        if (!timeRangePrices.isNullOrEmpty()) {
            binding.layoutTimeRangePrices.visibility = View.VISIBLE

            // 요일별로 그룹핑
            val groupedByDay = timeRangePrices.groupBy { it.dayOfWeek }

            groupedByDay.forEach { (dayOfWeek, prices) ->
                prices.forEach { timeRangePrice ->
                    val itemView = layoutInflater.inflate(
                        R.layout.item_time_range_price,
                        binding.containerTimeRangePrices,
                        false
                    )

                    val tvDayOfWeek = itemView.findViewById<TextView>(R.id.tvDayOfWeek)
                    val tvTimeRange = itemView.findViewById<TextView>(R.id.tvTimeRange)
                    val tvPrice = itemView.findViewById<TextView>(R.id.tvPrice)

                    // 요일 변환
                    val dayText = when (dayOfWeek) {
                        "MONDAY" -> "월"
                        "TUESDAY" -> "화"
                        "WEDNESDAY" -> "수"
                        "THURSDAY" -> "목"
                        "FRIDAY" -> "금"
                        "SATURDAY" -> "토"
                        "SUNDAY" -> "일"
                        else -> dayOfWeek ?: ""
                    }

                    tvDayOfWeek.text = dayText
                    tvTimeRange.text = "${timeRangePrice.startTime ?: ""} ~ ${timeRangePrice.endTime ?: ""}"
                    tvPrice.text = "${numberFormat.format(timeRangePrice.price ?: 0)}원"

                    binding.containerTimeRangePrices.addView(itemView)
                }
            }
        } else {
            binding.layoutTimeRangePrices.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(pricingPolicy: PricingPolicyDto): PriceDetailBottomSheet {
            return PriceDetailBottomSheet().apply {
                this.pricingPolicy = pricingPolicy
            }
        }
    }
}
