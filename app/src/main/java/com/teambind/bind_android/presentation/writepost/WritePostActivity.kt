package com.teambind.bind_android.presentation.writepost

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teambind.bind_android.databinding.ActivityWritePostBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.presentation.community.CategoryType
import com.teambind.bind_android.presentation.writepost.adapter.AttachedImageAdapter
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WritePostActivity : BaseActivity<ActivityWritePostBinding>() {

    private val viewModel: WritePostViewModel by viewModels()

    private val imageAdapter by lazy {
        AttachedImageAdapter { uri ->
            viewModel.removeImage(uri)
        }
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            viewModel.addImage(uri)
        }
    }

    override fun inflateBinding(): ActivityWritePostBinding {
        return ActivityWritePostBinding.inflate(layoutInflater)
    }

    override fun initView() {
        setupToolbar()
        setupInputs()
        setupImageList()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.btnClose.setOnSingleClickListener {
            showExitConfirmDialog()
        }

        binding.btnSubmit.setOnSingleClickListener {
            viewModel.submitPost()
        }
    }

    private fun setupInputs() {
        binding.etTitle.doAfterTextChanged { text ->
            viewModel.setTitle(text?.toString() ?: "")
        }

        binding.etContent.doAfterTextChanged { text ->
            viewModel.setContent(text?.toString() ?: "")
        }
    }

    private fun setupImageList() {
        binding.rvImages.adapter = imageAdapter
    }

    private fun setupClickListeners() {
        // Category selection
        binding.layoutCategory.setOnSingleClickListener {
            showCategoryDialog()
        }

        // Add image
        binding.btnAddImage.setOnSingleClickListener {
            val currentCount = viewModel.uiState.value.images.size
            if (currentCount >= 10) {
                showToast("이미지는 최대 10장까지 첨부할 수 있습니다")
            } else {
                imagePickerLauncher.launch("image/*")
            }
        }
    }

    private fun showCategoryDialog() {
        val categories = CategoryType.entries.filter { it != CategoryType.ALL }
        val categoryNames = categories.map { it.displayName }.toTypedArray()

        MaterialAlertDialogBuilder(this)
            .setTitle("게시판 선택")
            .setItems(categoryNames) { _, which ->
                viewModel.selectCategory(categories[which])
            }
            .show()
    }

    private fun showExitConfirmDialog() {
        val state = viewModel.uiState.value
        if (state.title.isNotBlank() || state.content.isNotBlank() || state.images.isNotEmpty()) {
            MaterialAlertDialogBuilder(this)
                .setTitle("작성 취소")
                .setMessage("작성 중인 내용이 있습니다. 나가시겠습니까?")
                .setPositiveButton("나가기") { _, _ -> finish() }
                .setNegativeButton("계속 작성", null)
                .show()
        } else {
            finish()
        }
    }

    override fun initObserver() {
        collectLatestFlow(viewModel.uiState) { state ->
            // Category
            binding.tvSelectedCategory.text = state.selectedCategory.displayName

            // Submit button state
            binding.btnSubmit.isEnabled = state.isSubmitEnabled
            binding.btnSubmit.alpha = if (state.isSubmitEnabled) 1f else 0.5f

            // Images
            imageAdapter.submitList(state.images)
            binding.rvImages.isVisible = state.images.isNotEmpty()
            binding.tvImageCount.text = "${state.images.size}/10"

            // Loading
            binding.layoutLoading.isVisible = state.isLoading

            // Success
            if (state.isSubmitSuccess) {
                showToast("게시글이 등록되었습니다")
                setResult(RESULT_OK)
                finish()
            }

            // Error
            state.errorMessage?.let {
                showToast(it)
                viewModel.clearError()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        showExitConfirmDialog()
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, WritePostActivity::class.java)
            context.startActivity(intent)
        }

        fun startForResult(context: android.app.Activity, requestCode: Int) {
            val intent = Intent(context, WritePostActivity::class.java)
            context.startActivityForResult(intent, requestCode)
        }
    }
}
