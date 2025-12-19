package com.teambind.bind_android.presentation.minifeed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.teambind.bind_android.R
import com.teambind.bind_android.data.model.response.FeedArticleDto
import com.teambind.bind_android.databinding.ItemMiniFeedBinding
import com.teambind.bind_android.util.extension.toRelativeTime

class MiniFeedAdapter(
    private val onItemClick: (FeedArticleDto) -> Unit
) : ListAdapter<FeedArticleDto, MiniFeedAdapter.MiniFeedViewHolder>(MiniFeedDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MiniFeedViewHolder {
        val binding = ItemMiniFeedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MiniFeedViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: MiniFeedViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MiniFeedViewHolder(
        private val binding: ItemMiniFeedBinding,
        private val onItemClick: (FeedArticleDto) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(article: FeedArticleDto) {
            with(binding) {
                // 게시판 이름 (텍스트만)
                tvBoardBadge.text = article.board.name

                // 제목
                tvTitle.text = article.title

                // 내용 미리보기
                tvContent.text = article.content

                // 작성자
                tvAuthor.text = article.writerName

                // 시간
                tvTime.text = article.createdAt.toRelativeTime()

                // 좋아요/댓글 수
                tvLikeCount.text = article.likeCount.toString()
                tvCommentCount.text = article.commentCount.toString()

                // 썸네일 이미지 (CardView)
                if (!article.firstImageUrl.isNullOrEmpty()) {
                    cardThumbnail.visibility = View.VISIBLE
                    Glide.with(ivThumbnail.context)
                        .load(article.firstImageUrl)
                        .centerCrop()
                        .into(ivThumbnail)
                } else {
                    cardThumbnail.visibility = View.GONE
                }

                // 프로필 이미지
                if (!article.writerProfileImage.isNullOrEmpty()) {
                    Glide.with(ivProfile.context)
                        .load(article.writerProfileImage)
                        .circleCrop()
                        .placeholder(R.drawable.bg_circle_gray)
                        .into(ivProfile)
                } else {
                    ivProfile.setImageResource(R.drawable.bg_circle_gray)
                }

                // 클릭 리스너
                root.setOnClickListener {
                    onItemClick(article)
                }
            }
        }
    }

    class MiniFeedDiffCallback : DiffUtil.ItemCallback<FeedArticleDto>() {
        override fun areItemsTheSame(oldItem: FeedArticleDto, newItem: FeedArticleDto): Boolean {
            return oldItem.articleId == newItem.articleId
        }

        override fun areContentsTheSame(oldItem: FeedArticleDto, newItem: FeedArticleDto): Boolean {
            return oldItem == newItem
        }
    }
}
