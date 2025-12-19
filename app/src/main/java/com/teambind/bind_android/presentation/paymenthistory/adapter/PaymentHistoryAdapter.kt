package com.teambind.bind_android.presentation.paymenthistory.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.teambind.bind_android.R
import com.teambind.bind_android.databinding.ItemPaymentHistoryBinding
import com.teambind.bind_android.presentation.paymenthistory.PaymentHistoryItem

class PaymentHistoryAdapter(
    private val onItemClick: (PaymentHistoryItem) -> Unit
) : ListAdapter<PaymentHistoryItem, PaymentHistoryAdapter.PaymentHistoryViewHolder>(PaymentHistoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentHistoryViewHolder {
        val binding = ItemPaymentHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PaymentHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaymentHistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PaymentHistoryViewHolder(
        private val binding: ItemPaymentHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(payment: PaymentHistoryItem) {
            with(binding) {
                tvPlaceName.text = payment.placeName
                tvRoomName.text = payment.roomName
                tvPaymentDate.text = payment.paymentDate.replace("-", ".")
                tvAmount.text = payment.displayAmount
                tvStatus.text = payment.displayStatus

                val statusColor = when (payment.status) {
                    "COMPLETED" -> R.color.primary_yellow
                    "CANCELLED", "REFUNDED" -> R.color.error_red
                    else -> R.color.gray_400
                }
                tvStatus.setTextColor(itemView.context.getColor(statusColor))

                root.setOnClickListener {
                    onItemClick(payment)
                }
            }
        }
    }

    class PaymentHistoryDiffCallback : DiffUtil.ItemCallback<PaymentHistoryItem>() {
        override fun areItemsTheSame(oldItem: PaymentHistoryItem, newItem: PaymentHistoryItem): Boolean {
            return oldItem.paymentId == newItem.paymentId
        }

        override fun areContentsTheSame(oldItem: PaymentHistoryItem, newItem: PaymentHistoryItem): Boolean {
            return oldItem == newItem
        }
    }
}
