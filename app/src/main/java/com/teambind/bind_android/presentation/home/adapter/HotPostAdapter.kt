package com.teambind.bind_android.presentation.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.teambind.bind_android.R
import com.teambind.bind_android.data.model.response.ArticleDto
import com.teambind.bind_android.databinding.ItemHotPostBinding

class HotPostAdapter(
    private val onItemClick: (ArticleDto) -> Unit
) : ListAdapter<ArticleDto, HotPostAdapter.HotPostViewHolder>(HotPostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotPostViewHolder {
        val binding = ItemHotPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HotPostViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: HotPostViewHolder, position: Int) {
        holder.bind(getItem(position), position == itemCount - 1)
    }

    class HotPostViewHolder(
        private val binding: ItemHotPostBinding,
        private val onItemClick: (ArticleDto) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(article: ArticleDto, isLastItem: Boolean) {
            with(binding) {
                // 마지막 아이템이면 오른쪽 마진 제거
                val layoutParams = root.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.marginEnd =
                    if (isLastItem) 0 else (10 * root.context.resources.displayMetrics.density).toInt()
                root.layoutParams = layoutParams

                // 카테고리 배지
                tvCategory.text = article.board.name

                // 제목
                tvTitle.text = article.title

                // 작성자
                tvAuthor.text = article.nickname

                // 좋아요/댓글 수
                tvLikeCount.text = article.likeCount.toString()
                tvCommentCount.text = article.commentCount.toString()

                // 프로필 이미지
                if (!article.profileImageUrl.isNullOrEmpty()) {
                    ivProfile.visibility = View.VISIBLE
                    Glide.with(ivProfile.context)
                        .load(article.profileImageUrl)
                        .circleCrop()
                        .placeholder(R.drawable.bg_circle_gray)
                        .into(ivProfile)
                } else {
                    ivProfile.visibility = View.GONE
                }

                // 클릭 리스너
                root.setOnClickListener {
                    onItemClick(article)
                }
            }
        }
    }

    class HotPostDiffCallback : DiffUtil.ItemCallback<ArticleDto>() {
        override fun areItemsTheSame(oldItem: ArticleDto, newItem: ArticleDto): Boolean {
            return oldItem.articleId == newItem.articleId
        }

        override fun areContentsTheSame(oldItem: ArticleDto, newItem: ArticleDto): Boolean {
            return oldItem == newItem
        }
    }
}
