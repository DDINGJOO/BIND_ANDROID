package com.teambind.bind_android.presentation.inquirydetail

import android.content.Context
import android.content.Intent
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.teambind.bind_android.databinding.ActivityInquiryDetailBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.util.extension.collectLatestFlow
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class InquiryDetailActivity : BaseActivity<ActivityInquiryDetailBinding>() {

    private val viewModel: InquiryDetailViewModel by viewModels()

    private val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREA)
    private val outputFormat = SimpleDateFormat("yy.MM.dd HH:mm", Locale.KOREA)

    override fun inflateBinding(): ActivityInquiryDetailBinding {
        return ActivityInquiryDetailBinding.inflate(layoutInflater)
    }

    override fun initView() {
        val inquiryId = intent.getStringExtra(EXTRA_INQUIRY_ID)
        if (inquiryId.isNullOrEmpty()) {
            showToast("잘못된 접근입니다.")
            finish()
            return
        }

        setupToolbar()
        viewModel.loadInquiryDetail(inquiryId)
    }

    override fun initObserver() {
        collectLatestFlow(viewModel.uiState) { state ->
            binding.layoutLoading.isVisible = state.isLoading

            state.inquiry?.let { inquiry ->
                // 카테고리
                binding.tvCategory.text = inquiry.getCategoryDisplayName()

                // 제목
                binding.tvTitle.text = inquiry.title

                // 작성일
                binding.tvDate.text = formatDate(inquiry.createdAt)

                // 내용
                binding.tvContent.text = inquiry.contents

                // 답변 여부에 따라 UI 분기
                if (inquiry.isAnswered) {
                    binding.layoutAnswer.isVisible = true
                    binding.layoutWaiting.isVisible = false
                    binding.tvAnswer.text = inquiry.answer ?: ""
                    binding.tvAnswerDate.text = inquiry.answeredAt?.let { formatDate(it) } ?: ""
                } else {
                    binding.layoutAnswer.isVisible = false
                    binding.layoutWaiting.isVisible = true
                }
            }

            state.errorMessage?.let {
                showToast(it)
                viewModel.clearError()
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }

    companion object {
        private const val EXTRA_INQUIRY_ID = "inquiry_id"

        fun start(context: Context, inquiryId: String) {
            val intent = Intent(context, InquiryDetailActivity::class.java).apply {
                putExtra(EXTRA_INQUIRY_ID, inquiryId)
            }
            context.startActivity(intent)
        }
    }
}
