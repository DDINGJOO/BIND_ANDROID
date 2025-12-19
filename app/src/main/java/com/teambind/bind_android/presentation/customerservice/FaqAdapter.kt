package com.teambind.bind_android.presentation.customerservice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.teambind.bind_android.data.model.response.FaqDto
import com.teambind.bind_android.databinding.ItemFaqBinding

class FaqAdapter : ListAdapter<FaqDto, FaqAdapter.FaqViewHolder>(FaqDiffCallback()) {

    private val expandedPositions = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val binding = ItemFaqBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FaqViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        holder.bind(getItem(position), expandedPositions.contains(position))
    }

    inner class FaqViewHolder(
        private val binding: ItemFaqBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.layoutQuestion.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    if (expandedPositions.contains(position)) {
                        expandedPositions.remove(position)
                    } else {
                        expandedPositions.add(position)
                    }
                    notifyItemChanged(position)
                }
            }
        }

        fun bind(faq: FaqDto, isExpanded: Boolean) {
            binding.tvQuestion.text = faq.question
            binding.tvAnswer.text = faq.answer

            if (isExpanded) {
                binding.layoutAnswer.visibility = View.VISIBLE
                binding.ivArrow.rotation = 180f
            } else {
                binding.layoutAnswer.visibility = View.GONE
                binding.ivArrow.rotation = 0f
            }
        }
    }

    class FaqDiffCallback : DiffUtil.ItemCallback<FaqDto>() {
        override fun areItemsTheSame(oldItem: FaqDto, newItem: FaqDto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FaqDto, newItem: FaqDto): Boolean {
            return oldItem == newItem
        }
    }
}
