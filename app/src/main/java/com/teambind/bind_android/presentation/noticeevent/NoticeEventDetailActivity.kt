package com.teambind.bind_android.presentation.noticeevent

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import com.teambind.bind_android.R
import com.teambind.bind_android.databinding.ActivityNoticeEventDetailBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoticeEventDetailActivity : BaseActivity<ActivityNoticeEventDetailBinding>() {

    override fun inflateBinding(): ActivityNoticeEventDetailBinding {
        return ActivityNoticeEventDetailBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        setupToolbar()
        loadData()
    }

    private fun setupToolbar() {
        val type = intent.getStringExtra(EXTRA_TYPE) ?: "NOTICE"
        binding.toolbar.title = if (type == "EVENT") "이벤트" else "공지사항"

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadData() {
        val type = intent.getStringExtra(EXTRA_TYPE) ?: "NOTICE"
        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        val content = intent.getStringExtra(EXTRA_CONTENT) ?: ""
        val date = intent.getStringExtra(EXTRA_DATE) ?: ""

        // Badge
        if (type == "EVENT") {
            binding.tvBadge.text = "이벤트"
            binding.tvBadge.setBackgroundResource(R.drawable.bg_badge_event)
        } else {
            binding.tvBadge.text = "공지"
            binding.tvBadge.setBackgroundResource(R.drawable.bg_badge_notice)
        }

        binding.tvTitle.text = title
        binding.tvDate.text = date
        binding.tvContent.text = content
    }

    companion object {
        private const val EXTRA_ID = "extra_id"
        private const val EXTRA_TYPE = "extra_type"
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_CONTENT = "extra_content"
        private const val EXTRA_DATE = "extra_date"

        fun createIntent(
            context: Context,
            id: Long,
            type: String,
            title: String,
            content: String,
            date: String
        ): Intent {
            return Intent(context, NoticeEventDetailActivity::class.java).apply {
                putExtra(EXTRA_ID, id)
                putExtra(EXTRA_TYPE, type)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_CONTENT, content)
                putExtra(EXTRA_DATE, date)
            }
        }
    }
}
