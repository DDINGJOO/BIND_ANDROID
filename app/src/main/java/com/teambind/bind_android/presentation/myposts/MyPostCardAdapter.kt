package com.teambind.bind_android.presentation.myposts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.teambind.bind_android.R
import com.teambind.bind_android.data.model.response.ArticleDto
import com.teambind.bind_android.databinding.ItemMyPostCardBinding
import com.teambind.bind_android.util.extension.toRelativeTime

class MyPostCardAdapter(
    private val onItemClick: (ArticleDto) -> Unit
) : ListAdapter<ArticleDto, MyPostCardAdapter.MyPostCardViewHolder>(MyPostCardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPostCardViewHolder {
        val binding = ItemMyPostCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyPostCardViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: MyPostCardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MyPostCardViewHolder(
        private val binding: ItemMyPostCardBinding,
        private val onItemClick: (ArticleDto) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(article: ArticleDto) {
            with(binding) {
                // Category badge
                tvCategory.text = article.board.name

                // Title
                tvTitle.text = article.title

                // Content preview
                tvContent.text = article.content

                // Author name
                tvAuthor.text = article.nickname

                // Time (relative)
                tvTime.text = article.createdAt.toRelativeTime()

                // Like/Comment counts
                tvLikeCount.text = article.likeCount.toString()
                tvCommentCount.text = article.commentCount.toString()

                // Profile image
                if (!article.profileImageUrl.isNullOrEmpty()) {
                    Glide.with(ivProfile)
                        .load(article.profileImageUrl)
                        .placeholder(R.drawable.bg_circle_gray)
                        .circleCrop()
                        .into(ivProfile)
                } else {
                    ivProfile.setImageResource(R.drawable.bg_circle_gray)
                }

                // Thumbnail image
                if (!article.thumbnailImageUrl.isNullOrEmpty()) {
                    cardThumbnail.visibility = View.VISIBLE
                    Glide.with(ivThumbnail)
                        .load(article.thumbnailImageUrl)
                        .centerCrop()
                        .into(ivThumbnail)
                } else {
                    cardThumbnail.visibility = View.GONE
                }

                // Click listener
                root.setOnClickListener {
                    onItemClick(article)
                }
            }
        }
    }

    class MyPostCardDiffCallback : DiffUtil.ItemCallback<ArticleDto>() {
        override fun areItemsTheSame(oldItem: ArticleDto, newItem: ArticleDto): Boolean {
            return oldItem.articleId == newItem.articleId
        }

        override fun areContentsTheSame(oldItem: ArticleDto, newItem: ArticleDto): Boolean {
            return oldItem == newItem
        }
    }
}
