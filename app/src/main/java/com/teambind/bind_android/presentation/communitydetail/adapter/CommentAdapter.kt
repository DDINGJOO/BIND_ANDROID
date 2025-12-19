package com.teambind.bind_android.presentation.communitydetail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.teambind.bind_android.R
import com.teambind.bind_android.data.model.response.CommentDto
import com.teambind.bind_android.databinding.ItemCommentBinding
import com.teambind.bind_android.util.extension.toRelativeTime

class CommentAdapter(
    private val onReplyClick: (CommentDto) -> Unit
) : ListAdapter<CommentDto, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CommentViewHolder(
        private val binding: ItemCommentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val replyAdapter by lazy {
            ReplyAdapter()
        }

        fun bind(comment: CommentDto) {
            with(binding) {
                // 프로필 이미지
                if (!comment.writerProfileImage.isNullOrEmpty()) {
                    Glide.with(ivProfile.context)
                        .load(comment.writerProfileImage)
                        .placeholder(R.drawable.bg_circle_gray)
                        .circleCrop()
                        .into(ivProfile)
                } else {
                    ivProfile.setImageResource(R.drawable.bg_circle_gray)
                }

                // 닉네임
                tvNickname.text = comment.writerName

                // 작성 시간
                tvCreatedAt.text = comment.createdAt.toRelativeTime()

                // 댓글 내용
                tvContent.text = comment.content

                // 답글 달기 클릭
                tvReply.setOnClickListener {
                    onReplyClick(comment)
                }

                // 대댓글이 있으면 표시
                if (!comment.replies.isNullOrEmpty()) {
                    rvReplies.isVisible = true
                    rvReplies.layoutManager = LinearLayoutManager(rvReplies.context)
                    rvReplies.adapter = replyAdapter
                    replyAdapter.submitList(comment.replies)
                } else {
                    rvReplies.isVisible = false
                }
            }
        }
    }

    class CommentDiffCallback : DiffUtil.ItemCallback<CommentDto>() {
        override fun areItemsTheSame(oldItem: CommentDto, newItem: CommentDto): Boolean {
            return oldItem.commentId == newItem.commentId
        }

        override fun areContentsTheSame(oldItem: CommentDto, newItem: CommentDto): Boolean {
            return oldItem == newItem
        }
    }
}

// 대댓글용 간단한 어댑터
class ReplyAdapter : ListAdapter<CommentDto, ReplyAdapter.ReplyViewHolder>(
    object : DiffUtil.ItemCallback<CommentDto>() {
        override fun areItemsTheSame(oldItem: CommentDto, newItem: CommentDto): Boolean {
            return oldItem.commentId == newItem.commentId
        }

        override fun areContentsTheSame(oldItem: CommentDto, newItem: CommentDto): Boolean {
            return oldItem == newItem
        }
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReplyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReplyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReplyViewHolder(
        private val binding: ItemCommentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(reply: CommentDto) {
            with(binding) {
                // 프로필 이미지
                if (!reply.writerProfileImage.isNullOrEmpty()) {
                    Glide.with(ivProfile.context)
                        .load(reply.writerProfileImage)
                        .placeholder(R.drawable.bg_circle_gray)
                        .circleCrop()
                        .into(ivProfile)
                } else {
                    ivProfile.setImageResource(R.drawable.bg_circle_gray)
                }

                tvNickname.text = reply.writerName
                tvCreatedAt.text = reply.createdAt.toRelativeTime()
                tvContent.text = reply.content
                tvReply.isVisible = false
                rvReplies.isVisible = false
            }
        }
    }
}
