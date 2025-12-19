package com.teambind.bind_android.presentation.communitydetail

import android.content.Context
import android.content.Intent
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.teambind.bind_android.R
import com.teambind.bind_android.data.model.response.ArticleDetailDto
import com.teambind.bind_android.data.model.response.CommentDto
import com.teambind.bind_android.databinding.ActivityCommunityDetailBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.presentation.communitydetail.adapter.ArticleImageAdapter
import com.teambind.bind_android.presentation.communitydetail.adapter.CommentAdapter
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import com.teambind.bind_android.util.extension.toDateFormat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommunityDetailActivity : BaseActivity<ActivityCommunityDetailBinding>() {

    private val viewModel: CommunityDetailViewModel by viewModels()

    private val commentAdapter by lazy {
        CommentAdapter { comment ->
            onReplyClick(comment)
        }
    }

    private val imageAdapter by lazy {
        ArticleImageAdapter { _, _ ->
            // TODO: 이미지 확대 보기
        }
    }

    private var replyToCommentId: String? = null
    private var articleId: String? = null

    override fun inflateBinding(): ActivityCommunityDetailBinding {
        return ActivityCommunityDetailBinding.inflate(layoutInflater)
    }

    override fun initView() {
        articleId = intent.getStringExtra(EXTRA_ARTICLE_ID)
        if (articleId.isNullOrEmpty()) {
            showToast("게시글 정보를 찾을 수 없습니다.")
            finish()
            return
        }

        setupToolbar()
        setupImageList()
        setupCommentList()
        setupClickListeners()
        setupCommentInput()
        viewModel.loadArticleDetail(articleId!!)
    }

    private fun setupToolbar() {
        binding.btnBack.setOnSingleClickListener {
            finish()
        }

        binding.btnMore.setOnSingleClickListener {
            showReportBottomSheet()
        }
    }

    private fun showReportBottomSheet() {
        val bottomSheet = ReportBottomSheet.newInstance()
        bottomSheet.setOnReportSubmitListener { category, reason ->
            viewModel.reportArticle(category, reason)
        }
        bottomSheet.show(supportFragmentManager, ReportBottomSheet.TAG)
    }

    private fun setupImageList() {
        binding.rvImages.adapter = imageAdapter
    }

    private fun setupCommentList() {
        binding.rvComments.adapter = commentAdapter
    }

    private fun setupClickListeners() {
        with(binding) {
            // 좋아요
            layoutLike.setOnSingleClickListener {
                viewModel.toggleLike()
            }

            // 댓글 전송
            btnSendComment.setOnSingleClickListener {
                sendComment()
            }
        }
    }

    private fun setupCommentInput() {
        binding.etComment.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendComment()
                true
            } else {
                false
            }
        }
    }

    private fun sendComment() {
        val content = binding.etComment.text.toString().trim()
        if (content.isNotEmpty()) {
            viewModel.sendComment(content, replyToCommentId)
        }
    }

    override fun initObserver() {
        collectLatestFlow(viewModel.uiState) { state ->
            // 게시글 정보 업데이트
            state.article?.let { article ->
                updateArticleInfo(article, state.likeCount, state.comments.size)
            }

            // 댓글 목록 업데이트
            commentAdapter.submitList(state.comments)
            binding.tvEmptyComments.isVisible = state.comments.isEmpty()

            // 좋아요 상태
            binding.ivLike.setImageResource(
                if (state.isLiked) R.drawable.ic_like_filled
                else R.drawable.ic_like_empty
            )
            binding.tvLikeCount.text = state.likeCount.toString()

            // 댓글 전송 완료
            if (state.commentSent) {
                binding.etComment.setText("")
                replyToCommentId = null
                binding.etComment.hint = "댓글을 입력해주세요"
                viewModel.clearCommentSent()
            }

            // 신고 완료
            if (state.reportSuccess) {
                viewModel.clearReportSuccess()
            }

            // 에러 메시지
            state.errorMessage?.let {
                showToast(it)
                viewModel.clearError()
            }
        }
    }

    private fun updateArticleInfo(article: ArticleDetailDto, likeCount: Int, commentCount: Int) {
        with(binding) {
            // 카테고리
            tvCategory.text = article.board.name

            // 제목
            tvTitle.text = article.title

            // 작성자 프로필
            if (!article.profileImageUrl.isNullOrEmpty()) {
                Glide.with(this@CommunityDetailActivity)
                    .load(article.profileImageUrl)
                    .placeholder(R.drawable.bg_circle_gray)
                    .circleCrop()
                    .into(ivProfile)
            }

            // 작성자 닉네임
            tvNickname.text = article.nickname

            // 작성 시간 (년.월.일 형식)
            tvCreatedAt.text = article.createdAt.toDateFormat()

            // 본문
            tvContent.text = article.content

            // 통계
            tvLikeCount.text = likeCount.toString()
            tvCommentCount.text = commentCount.toString()
            tvViewCount.text = "조회 ${article.viewCount}"

            // 이미지가 있으면 표시
            if (article.images.isNotEmpty()) {
                rvImages.isVisible = true
                imageAdapter.submitList(article.images.sortedBy { it.sequence })
            } else {
                rvImages.isVisible = false
            }
        }
    }

    private fun onReplyClick(comment: CommentDto) {
        replyToCommentId = comment.commentId
        binding.etComment.hint = "${comment.writerName}님에게 답글 작성..."
        binding.etComment.requestFocus()
    }

    companion object {
        private const val EXTRA_ARTICLE_ID = "extra_article_id"

        fun start(context: Context, articleId: String) {
            val intent = Intent(context, CommunityDetailActivity::class.java).apply {
                putExtra(EXTRA_ARTICLE_ID, articleId)
            }
            context.startActivity(intent)
        }
    }
}
