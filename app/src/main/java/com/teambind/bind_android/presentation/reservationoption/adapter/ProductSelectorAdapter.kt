package com.teambind.bind_android.presentation.reservationoption.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.teambind.bind_android.R
import com.teambind.bind_android.data.model.response.ProductDto
import com.teambind.bind_android.databinding.ItemProductSelectorBinding
import com.teambind.bind_android.presentation.reservationoption.SelectedProduct
import java.text.NumberFormat
import java.util.Locale

class ProductSelectorAdapter(
    private val products: List<ProductDto>,
    private val initialSelectedProducts: List<SelectedProduct>,
    private val onQuantityChanged: () -> Unit
) : RecyclerView.Adapter<ProductSelectorAdapter.ProductViewHolder>() {

    private val quantities = mutableMapOf<Long, Int>().apply {
        initialSelectedProducts.forEach { selected ->
            put(selected.productId, selected.quantity)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductSelectorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    fun getSelectedProducts(): List<SelectedProduct> {
        return quantities.filter { it.value > 0 }.mapNotNull { (productId, quantity) ->
            products.find { it.productId == productId }?.let { product ->
                val price = product.pricingStrategy?.initialPrice ?: 0
                SelectedProduct(
                    productId = product.productId,
                    productName = product.name ?: "",
                    quantity = quantity,
                    unitPrice = price
                )
            }
        }
    }

    inner class ProductViewHolder(
        private val binding: ItemProductSelectorBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ProductDto) {
            val currentQuantity = quantities[product.productId] ?: 0
            val formatter = NumberFormat.getNumberInstance(Locale.KOREA)
            val price = product.pricingStrategy?.initialPrice ?: 0
            val maxQuantity = product.totalQuantity ?: 10

            with(binding) {
                tvProductName.text = product.name ?: ""
                tvProductPrice.text = "${formatter.format(price)}원"
                tvProductStock.text = "(재고 ${maxQuantity}개)"
                tvQuantity.text = currentQuantity.toString()

                ivProduct.setImageResource(R.drawable.bg_rounded_rect)

                btnMinus.setOnClickListener {
                    val newQuantity = (quantities[product.productId] ?: 0) - 1
                    if (newQuantity >= 0) {
                        quantities[product.productId] = newQuantity
                        tvQuantity.text = newQuantity.toString()
                        onQuantityChanged()
                    }
                }

                btnPlus.setOnClickListener {
                    val newQuantity = (quantities[product.productId] ?: 0) + 1
                    if (newQuantity <= maxQuantity) {
                        quantities[product.productId] = newQuantity
                        tvQuantity.text = newQuantity.toString()
                        onQuantityChanged()
                    }
                }

                // Update button states
                btnMinus.alpha = if (currentQuantity > 0) 1f else 0.5f
                btnPlus.alpha = if (currentQuantity < maxQuantity) 1f else 0.5f
            }
        }
    }
}
