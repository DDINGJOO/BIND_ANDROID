package com.teambind.bind_android.presentation.reservationoption

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.teambind.bind_android.data.model.response.ProductDto
import com.teambind.bind_android.databinding.BottomSheetProductSelectorBinding
import com.teambind.bind_android.presentation.reservationoption.adapter.ProductSelectorAdapter

class ProductSelectorBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetProductSelectorBinding? = null
    private val binding get() = _binding!!

    private var availableProducts: List<ProductDto> = emptyList()
    private var selectedProducts: List<SelectedProduct> = emptyList()
    private var onProductsSelected: ((List<SelectedProduct>) -> Unit)? = null

    private val productAdapter by lazy {
        ProductSelectorAdapter(
            products = availableProducts,
            initialSelectedProducts = selectedProducts,
            onQuantityChanged = { /* We'll handle this on confirm */ }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetProductSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        binding.rvProducts.apply {
            adapter = productAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.btnClose.setOnClickListener {
            // X 버튼 클릭 시에도 선택한 상품 저장
            val selectedProducts = productAdapter.getSelectedProducts()
            onProductsSelected?.invoke(selectedProducts)
            dismiss()
        }

        binding.btnConfirm.setOnClickListener {
            val selectedProducts = productAdapter.getSelectedProducts()
            onProductsSelected?.invoke(selectedProducts)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "ProductSelectorBottomSheet"

        fun show(
            fragmentManager: FragmentManager,
            availableProducts: List<ProductDto>,
            selectedProducts: List<SelectedProduct>,
            onProductsSelected: (List<SelectedProduct>) -> Unit
        ) {
            val bottomSheet = ProductSelectorBottomSheet().apply {
                this.availableProducts = availableProducts
                this.selectedProducts = selectedProducts
                this.onProductsSelected = onProductsSelected
            }
            bottomSheet.show(fragmentManager, TAG)
        }
    }
}
