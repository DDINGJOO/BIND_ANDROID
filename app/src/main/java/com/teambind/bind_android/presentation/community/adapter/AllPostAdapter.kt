package com.teambind.bind_android.presentation.community.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.teambind.bind_android.R
import com.teambind.bind_android.data.model.response.ArticleDto
import com.teambind.bind_android.databinding.ItemAllPostBinding
import com.teambind.bind_android.util.extension.toRelativeTime

class AllPostAdapter(
    private val onItemClick: (ArticleDto) -> Unit
) : ListAdapter<ArticleDto, AllPostAdapter.AllPostViewHolder>(AllPostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllPostViewHolder {
        val binding = ItemAllPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AllPostViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: AllPostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AllPostViewHolder(
        private val binding: ItemAllPostBinding,
        private val onItemClick: (ArticleDto) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(article: ArticleDto) {
            with(binding) {
                // 게시판 이름 (상단)
                tvCategory.text = article.board.name

                // 제목
                tvTitle.text = article.title

                // 내용 미리보기
                tvContent.text = article.content

                // 작성자 이름
                tvAuthor.text = article.nickname

                // 작성 시간 (상대 시간)
                tvTime.text = article.createdAt.toRelativeTime()

                // 좋아요/댓글 수
                tvLikeCount.text = article.likeCount.toString()
                tvCommentCount.text = article.commentCount.toString()

                // 프로필 이미지
                if (!article.profileImageUrl.isNullOrEmpty()) {
                    Glide.with(ivProfile)
                        .load(article.profileImageUrl)
                        .placeholder(R.drawable.bg_circle_gray)
                        .circleCrop()
                        .into(ivProfile)
                } else {
                    ivProfile.setImageResource(R.drawable.bg_circle_gray)
                }

                // 썸네일 이미지 (CardView)
                if (!article.thumbnailImageUrl.isNullOrEmpty()) {
                    cardThumbnail.visibility = View.VISIBLE
                    Glide.with(ivThumbnail)
                        .load(article.thumbnailImageUrl)
                        .centerCrop()
                        .into(ivThumbnail)
                } else {
                    cardThumbnail.visibility = View.GONE
                }

                // 클릭 리스너
                root.setOnClickListener {
                    onItemClick(article)
                }
            }
        }
    }

    class AllPostDiffCallback : DiffUtil.ItemCallback<ArticleDto>() {
        override fun areItemsTheSame(oldItem: ArticleDto, newItem: ArticleDto): Boolean {
            return oldItem.articleId == newItem.articleId
        }

        override fun areContentsTheSame(oldItem: ArticleDto, newItem: ArticleDto): Boolean {
            return oldItem == newItem
        }
    }
}
