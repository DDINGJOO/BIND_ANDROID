package com.teambind.bind_android.presentation.find

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.teambind.bind_android.R
import com.teambind.bind_android.data.model.response.StudioDto
import com.teambind.bind_android.databinding.BottomSheetStudioPreviewBinding
import com.teambind.bind_android.presentation.studiodetail.StudioDetailActivity

class StudioPreviewBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetStudioPreviewBinding? = null
    private val binding get() = _binding!!

    private var studio: StudioDto? = null

    override fun getTheme(): Int = R.style.Theme_BIND_ANDROID_BottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetStudioPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        studio?.let { bindData(it) }
        setupClickListeners()
    }

    private fun bindData(studio: StudioDto) {
        with(binding) {
            // 이미지
            Glide.with(this@StudioPreviewBottomSheet)
                .load(studio.thumbnailUrl)
                .placeholder(R.color.gray3)
                .error(R.color.gray3)
                .centerCrop()
                .into(ivStudio)

            // 카테고리
            tvCategory.text = studio.category ?: studio.placeType ?: ""

            // 이름
            tvName.text = studio.name ?: "스튜디오"

            // 평점
            tvRating.text = String.format("%.1f", studio.rating ?: 0.0)
            tvReviewCount.text = "(${studio.reviewCount ?: 0})"

            // 주소
            tvAddress.text = studio.address ?: ""
        }
    }

    private fun setupClickListeners() {
        binding.btnViewDetail.setOnClickListener {
            studio?.studioId?.let { id ->
                val intent = Intent(requireContext(), StudioDetailActivity::class.java).apply {
                    putExtra("placeId", id)
                }
                startActivity(intent)
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "StudioPreviewBottomSheet"

        fun newInstance(studio: StudioDto): StudioPreviewBottomSheet {
            return StudioPreviewBottomSheet().apply {
                this.studio = studio
            }
        }
    }
}
