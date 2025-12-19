package com.teambind.bind_android.util.extension

import android.util.Patterns
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

fun String.isValidEmail(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isValidPhone(): Boolean {
    val phonePattern = Pattern.compile("^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$")
    return phonePattern.matcher(this).matches()
}

fun String.isValidPassword(): Boolean {
    // 최소 8자, 영문 대소문자, 숫자, 특수문자 포함
    val passwordPattern = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
    )
    return passwordPattern.matcher(this).matches()
}

fun String.isValidSimplePassword(): Boolean {
    // 최소 8자, 영문과 숫자 포함
    val passwordPattern = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$"
    )
    return passwordPattern.matcher(this).matches()
}

fun String.formatPhoneNumber(): String {
    val digits = this.replace("-", "").replace(" ", "")
    return when {
        digits.length == 11 -> "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7)}"
        digits.length == 10 -> "${digits.substring(0, 3)}-${digits.substring(3, 6)}-${digits.substring(6)}"
        else -> this
    }
}

fun String.removeHtmlTags(): String {
    return this.replace(Regex("<[^>]*>"), "")
}

fun String?.orEmpty(default: String = ""): String {
    return this ?: default
}

fun String?.toIntOrDefault(default: Int = 0): Int {
    return this?.toIntOrNull() ?: default
}

fun String?.toLongOrDefault(default: Long = 0L): Long {
    return this?.toLongOrNull() ?: default
}

fun String?.toDoubleOrDefault(default: Double = 0.0): Double {
    return this?.toDoubleOrNull() ?: default
}

fun Int.toFormattedPrice(): String {
    val formatter = DecimalFormat("#,###")
    return formatter.format(this) + "원"
}

fun Long.toFormattedPrice(): String {
    val formatter = DecimalFormat("#,###")
    return formatter.format(this) + "원"
}

fun Long.toDateString(pattern: String = "yyyy-MM-dd"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(this))
}

fun String.toDate(pattern: String = "yyyy-MM-dd"): Date? {
    return try {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        sdf.parse(this)
    } catch (e: Exception) {
        null
    }
}

/**
 * ISO 8601 형식의 날짜 문자열을 yyyy.MM.dd 형식으로 변환
 * 예: "2025-12-17T10:30:00Z" -> "2025.12.17"
 */
fun String.toDateFormat(): String {
    return try {
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd HH:mm:ss"
        )

        var date: Date? = null
        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.getDefault())
                date = sdf.parse(this)
                if (date != null) break
            } catch (e: Exception) {
                continue
            }
        }

        if (date == null) return this

        val outputFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        outputFormat.format(date)
    } catch (e: Exception) {
        this
    }
}

/**
 * ISO 8601 형식의 날짜 문자열을 상대 시간으로 변환
 * 예: "2025-12-17T10:30:00Z" -> "2시간 전"
 */
fun String.toRelativeTime(): String {
    return try {
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd HH:mm:ss"
        )

        var date: Date? = null
        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.getDefault())
                sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                date = sdf.parse(this)
                if (date != null) break
            } catch (e: Exception) {
                continue
            }
        }

        if (date == null) return this

        val now = System.currentTimeMillis()
        val diff = now - date.time
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        when {
            seconds < 60 -> "방금 전"
            minutes < 60 -> "${minutes}분 전"
            hours < 24 -> "${hours}시간 전"
            else -> {
                // 24시간 이후는 년.월.일 형식으로 표시
                val outputFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                outputFormat.format(date)
            }
        }
    } catch (e: Exception) {
        this
    }
}
