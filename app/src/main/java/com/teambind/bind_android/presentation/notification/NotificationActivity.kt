package com.teambind.bind_android.presentation.notification

import android.content.Context
import android.content.Intent
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.teambind.bind_android.databinding.ActivityNotificationBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.presentation.notification.adapter.NotificationAdapter
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationActivity : BaseActivity<ActivityNotificationBinding>() {

    private val viewModel: NotificationViewModel by viewModels()

    private val notificationAdapter by lazy {
        NotificationAdapter { notification ->
            viewModel.markAsRead(notification.id)
        }
    }

    override fun inflateBinding(): ActivityNotificationBinding {
        return ActivityNotificationBinding.inflate(layoutInflater)
    }

    override fun initView() {
        setupRecyclerView()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        binding.rvNotifications.apply {
            adapter = notificationAdapter
            layoutManager = LinearLayoutManager(this@NotificationActivity)
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnSingleClickListener {
            finish()
        }
    }

    override fun initObserver() {
        collectLatestFlow(viewModel.uiState) { state ->
            // Notifications
            notificationAdapter.submitList(state.notifications)

            // Empty state
            binding.layoutEmpty.isVisible = !state.isLoading && state.notifications.isEmpty()
            binding.rvNotifications.isVisible = state.notifications.isNotEmpty()

            // Loading
            binding.progressBar.isVisible = state.isLoading

            // Error
            state.errorMessage?.let {
                showToast(it)
                viewModel.clearError()
            }
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, NotificationActivity::class.java)
            context.startActivity(intent)
        }
    }
}
