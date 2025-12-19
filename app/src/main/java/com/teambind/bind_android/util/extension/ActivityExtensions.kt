package com.teambind.bind_android.util.extension

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

fun Activity.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Activity.showToast(@StringRes messageResId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, messageResId, duration).show()
}

fun Activity.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

inline fun <reified T> Activity.startActivity(
    noinline intentBuilder: Intent.() -> Unit = {}
) {
    val intent = Intent(this, T::class.java).apply(intentBuilder)
    startActivity(intent)
}

inline fun <reified T> Activity.startActivityAndFinish(
    noinline intentBuilder: Intent.() -> Unit = {}
) {
    startActivity<T>(intentBuilder)
    finish()
}

inline fun <reified T> Activity.startActivityClearTask(
    noinline intentBuilder: Intent.() -> Unit = {}
) {
    val intent = Intent(this, T::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intentBuilder()
    }
    startActivity(intent)
}

fun Activity.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Activity.hideKeyboard() {
    val view = currentFocus ?: View(this)
    hideKeyboard(view)
}

fun Activity.showKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    view.requestFocus()
    inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

fun Activity.setStatusBarColor(color: Int, isLight: Boolean = false) {
    window.statusBarColor = color
    WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = isLight
}

fun Activity.setNavigationBarColor(color: Int, isLight: Boolean = false) {
    window.navigationBarColor = color
    WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = isLight
}

fun Activity.setFullScreen() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
}

fun <T> AppCompatActivity.collectFlow(flow: Flow<T>, action: suspend (T) -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect { action(it) }
        }
    }
}

fun <T> AppCompatActivity.collectLatestFlow(flow: Flow<T>, action: suspend (T) -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collectLatest { action(it) }
        }
    }
}
