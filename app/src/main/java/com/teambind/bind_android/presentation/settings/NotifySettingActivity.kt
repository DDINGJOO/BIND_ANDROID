package com.teambind.bind_android.presentation.settings

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import com.teambind.bind_android.databinding.ActivityNotifySettingBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotifySettingActivity : BaseActivity<ActivityNotifySettingBinding>() {

    override fun inflateBinding(): ActivityNotifySettingBinding {
        return ActivityNotifySettingBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        setupToolbar()
        setupSwitches()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupSwitches() {
        // 알림 설정 변경 시 SharedPreferences에 저장
        binding.switchReservation.setOnCheckedChangeListener { _, isChecked ->
            // TODO: 서버 API 또는 SharedPreferences에 저장
        }

        binding.switchCommunity.setOnCheckedChangeListener { _, isChecked ->
            // TODO: 서버 API 또는 SharedPreferences에 저장
        }

        binding.switchMarketing.setOnCheckedChangeListener { _, isChecked ->
            // TODO: 서버 API 또는 SharedPreferences에 저장
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, NotifySettingActivity::class.java)
        }
    }
}
