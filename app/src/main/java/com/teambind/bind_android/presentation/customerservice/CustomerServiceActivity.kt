package com.teambind.bind_android.presentation.customerservice

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.teambind.bind_android.R
import com.teambind.bind_android.databinding.ActivityCustomerServiceBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomerServiceActivity : BaseActivity<ActivityCustomerServiceBinding>() {

    private val viewModel: CustomerServiceViewModel by viewModels()
    private val faqAdapter by lazy { FaqAdapter() }

    override fun inflateBinding(): ActivityCustomerServiceBinding {
        return ActivityCustomerServiceBinding.inflate(layoutInflater)
    }

    override fun initView() {
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnSingleClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        binding.rvFaq.apply {
            adapter = faqAdapter
            layoutManager = LinearLayoutManager(this@CustomerServiceActivity)
            itemAnimator = null
        }
    }

    private fun setupClickListeners() {
        binding.btnCall.setOnSingleClickListener {
            val phoneNumber = binding.tvPhone.text.toString().replace("-", "")
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            startActivity(intent)
        }

        binding.btnWriteInquiry.setOnSingleClickListener {
            startActivity(WriteInquiryActivity.createIntent(this))
        }
    }

    private fun setupCategoryTabs(categories: List<String>, selectedCategory: String) {
        binding.layoutCategoryTabs.removeAllViews()

        categories.forEach { category ->
            val tab = createCategoryTab(category, category == selectedCategory)
            tab.setOnClickListener {
                viewModel.selectCategory(category)
            }
            binding.layoutCategoryTabs.addView(tab)
        }
    }

    private fun createCategoryTab(category: String, isSelected: Boolean): TextView {
        return TextView(this).apply {
            text = category
            textSize = 14f
            typeface = resources.getFont(R.font.pretendard_semibold)
            gravity = Gravity.CENTER

            val horizontalPadding = (16 * resources.displayMetrics.density).toInt()
            val verticalPadding = (8 * resources.displayMetrics.density).toInt()
            setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)

            val marginEnd = (8 * resources.displayMetrics.density).toInt()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, marginEnd, 0)
            }

            if (isSelected) {
                setBackgroundResource(R.drawable.bg_badge_black)
                setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                setBackgroundResource(R.drawable.bg_category_badge)
                setTextColor(ContextCompat.getColor(context, R.color.gray_600))
            }
        }
    }

    override fun initObserver() {
        collectLatestFlow(viewModel.uiState) { state ->
            // Loading
            binding.progressBar.isVisible = state.isLoading

            // Categories
            if (state.categories.isNotEmpty()) {
                setupCategoryTabs(state.categories, state.selectedCategory)
            }

            // FAQ list
            faqAdapter.submitList(state.filteredFaqs)

            // Empty state
            binding.layoutEmpty.isVisible = !state.isLoading && state.filteredFaqs.isEmpty()
            binding.rvFaq.isVisible = state.filteredFaqs.isNotEmpty()

            // Error
            state.errorMessage?.let {
                showToast(it)
                viewModel.clearError()
            }
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, CustomerServiceActivity::class.java)
            context.startActivity(intent)
        }
    }
}
