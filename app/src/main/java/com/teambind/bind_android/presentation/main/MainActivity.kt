package com.teambind.bind_android.presentation.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.view.LayoutInflater
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.teambind.bind_android.R
import com.teambind.bind_android.data.api.interceptor.TokenRefreshAuthenticator
import com.teambind.bind_android.databinding.ActivityMainBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.presentation.start.auth.AuthMainActivity
import com.teambind.bind_android.util.extension.startActivityClearTask
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val logoutReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == TokenRefreshAuthenticator.ACTION_UNAUTHORIZED_LOGOUT) {
                showToast("세션이 만료되었습니다. 다시 로그인해주세요.")
                startActivityClearTask<AuthMainActivity>()
            }
        }
    }

    override fun inflateBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        setupBottomNavigation()
        registerLogoutReceiver()
    }

    private fun setupBottomNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)
    }

    private fun registerLogoutReceiver() {
        val intentFilter = IntentFilter(TokenRefreshAuthenticator.ACTION_UNAUTHORIZED_LOGOUT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(logoutReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(logoutReceiver, intentFilter)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(logoutReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver not registered
        }
    }
}
