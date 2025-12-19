package com.teambind.bind_android.presentation.reservationoption.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.teambind.bind_android.databinding.ItemSelectedProductBinding
import com.teambind.bind_android.presentation.reservationoption.SelectedProduct
import java.text.NumberFormat
import java.util.*

class SelectedProductAdapter :
    ListAdapter<SelectedProduct, SelectedProductAdapter.SelectedProductViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedProductViewHolder {
        val binding = ItemSelectedProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SelectedProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SelectedProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SelectedProductViewHolder(
        private val binding: ItemSelectedProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: SelectedProduct) {
            val formatter = NumberFormat.getNumberInstance(Locale.KOREA)
            val totalPrice = product.unitPrice * product.quantity

            with(binding) {
                tvProductName.text = product.productName
                tvQuantity.text = "x${product.quantity}"
                tvPrice.text = "${formatter.format(totalPrice)}원"
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SelectedProduct>() {
        override fun areItemsTheSame(oldItem: SelectedProduct, newItem: SelectedProduct): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: SelectedProduct, newItem: SelectedProduct): Boolean {
            return oldItem == newItem
        }
    }
}
