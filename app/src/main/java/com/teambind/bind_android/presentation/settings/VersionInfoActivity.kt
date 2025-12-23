package com.teambind.bind_android.presentation.settings

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import com.teambind.bind_android.BuildConfig
import com.teambind.bind_android.databinding.ActivityVersionInfoBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VersionInfoActivity : BaseActivity<ActivityVersionInfoBinding>() {

    override fun inflateBinding(): ActivityVersionInfoBinding {
        return ActivityVersionInfoBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        setupToolbar()
        setupVersionInfo()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupVersionInfo() {
        val versionName = BuildConfig.VERSION_NAME
        binding.tvVersion.text = "현재 버전 : v$versionName"
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, VersionInfoActivity::class.java)
        }
    }
}
