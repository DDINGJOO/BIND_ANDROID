package com.teambind.bind_android.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.teambind.bind_android.R
import com.teambind.bind_android.data.model.response.PlaceKeywordDto
import com.teambind.bind_android.data.repository.EnumsRepository
import com.teambind.bind_android.databinding.BottomSheetKeywordFilterBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class KeywordFilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetKeywordFilterBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var enumsRepository: EnumsRepository

    private var selectedKeywordIds: MutableSet<Long> = mutableSetOf()
    private var onKeywordsSelected: ((List<Long>) -> Unit)? = null
    private val keywordChipMap = mutableMapOf<Long, Chip>()

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetKeywordFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtons()
        loadKeywords()
    }

    private fun loadKeywords() {
        lifecycleScope.launch {
            enumsRepository.getPlaceKeywords()
                .onSuccess { keywords ->
                    displayKeywords(keywords)
                }
                .onFailure {
                    // 실패 시 기본 키워드 표시
                    displayDefaultKeywords()
                }
        }
    }

    private fun displayKeywords(keywords: List<PlaceKeywordDto>) {
        val groupedKeywords = keywords
            .sortedBy { it.displayOrder }
            .groupBy { it.type }

        binding.layoutKeywordSections.removeAllViews()

        val typeDisplayNames = mapOf(
            "SPACE_TYPE" to "공간 유형",
            "INSTRUMENT_EQUIPMENT" to "악기/장비",
            "CONVENIENCE" to "편의시설",
            "OTHER" to "기타 특성"
        )

        groupedKeywords.forEach { (type, keywordList) ->
            addSection(typeDisplayNames[type] ?: type, keywordList)
        }
    }

    private fun displayDefaultKeywords() {
        val defaultKeywords = listOf(
            PlaceKeywordDto(1, "합주실", "SPACE_TYPE", "밴드 합주용", 1),
            PlaceKeywordDto(2, "연습실", "SPACE_TYPE", "개인/그룹", 2),
            PlaceKeywordDto(3, "레슨실", "SPACE_TYPE", "1:1 또는 소규모", 3),
            PlaceKeywordDto(4, "녹음실", "SPACE_TYPE", "음원 녹음/믹싱", 4),
            PlaceKeywordDto(5, "드럼", "INSTRUMENT_EQUIPMENT", "드럼 장비", 5),
            PlaceKeywordDto(6, "기타 앰프", "INSTRUMENT_EQUIPMENT", "기타 앰프", 6),
            PlaceKeywordDto(7, "베이스 앰프", "INSTRUMENT_EQUIPMENT", "베이스 앰프", 7),
            PlaceKeywordDto(8, "키보드", "INSTRUMENT_EQUIPMENT", "키보드", 8),
            PlaceKeywordDto(9, "주차 가능", "CONVENIENCE", "주차", 9),
            PlaceKeywordDto(10, "와이파이", "CONVENIENCE", "와이파이", 10),
            PlaceKeywordDto(11, "에어컨", "CONVENIENCE", "에어컨", 11),
            PlaceKeywordDto(12, "24시간", "OTHER", "24시간 운영", 12)
        )
        displayKeywords(defaultKeywords)
    }

    private fun addSection(title: String, keywords: List<PlaceKeywordDto>) {
        val context = requireContext()

        // Section Title
        val titleView = TextView(context).apply {
            text = title
            setTextColor(resources.getColor(R.color.gray6, null))
            textSize = 14f
            typeface = resources.getFont(R.font.pretendard_semibold)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (16 * resources.displayMetrics.density).toInt()
                bottomMargin = (8 * resources.displayMetrics.density).toInt()
            }
        }
        binding.layoutKeywordSections.addView(titleView)

        // FlexboxLayout for chips
        val flexboxLayout = FlexboxLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            flexWrap = com.google.android.flexbox.FlexWrap.WRAP
            justifyContent = com.google.android.flexbox.JustifyContent.FLEX_START
        }

        keywords.forEach { keyword ->
            val chip = createKeywordChip(keyword)
            flexboxLayout.addView(chip)
            keywordChipMap[keyword.id] = chip
        }

        binding.layoutKeywordSections.addView(flexboxLayout)
    }

    private fun createKeywordChip(keyword: PlaceKeywordDto): Chip {
        return Chip(requireContext()).apply {
            text = "#${keyword.name}"
            isCheckable = true
            isChecked = selectedKeywordIds.contains(keyword.id)

            // 스타일 적용
            setTextAppearance(R.style.FilterChipStyle)
            chipBackgroundColor = resources.getColorStateList(R.color.chip_filter_background_selector, null)
            chipStrokeColor = resources.getColorStateList(R.color.chip_filter_stroke_selector, null)
            chipStrokeWidth = resources.displayMetrics.density
            chipCornerRadius = 10 * resources.displayMetrics.density
            chipMinHeight = (36 * resources.displayMetrics.density)
            chipStartPadding = 12 * resources.displayMetrics.density
            chipEndPadding = 12 * resources.displayMetrics.density
            isCheckedIconVisible = false
            textSize = 13f

            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = (6 * resources.displayMetrics.density).toInt()
                bottomMargin = (6 * resources.displayMetrics.density).toInt()
            }

            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedKeywordIds.add(keyword.id)
                } else {
                    selectedKeywordIds.remove(keyword.id)
                }
            }
        }
    }

    private fun setupButtons() {
        // 초기화 버튼
        binding.btnReset.setOnClickListener {
            selectedKeywordIds.clear()
            keywordChipMap.values.forEach { it.isChecked = false }
        }

        // 적용 버튼
        binding.btnApply.setOnClickListener {
            onKeywordsSelected?.invoke(selectedKeywordIds.toList())
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(
            currentKeywordIds: List<Long>?,
            onKeywordsSelected: (List<Long>) -> Unit
        ): KeywordFilterBottomSheet {
            return KeywordFilterBottomSheet().apply {
                this.selectedKeywordIds = currentKeywordIds?.toMutableSet() ?: mutableSetOf()
                this.onKeywordsSelected = onKeywordsSelected
            }
        }
    }
}
