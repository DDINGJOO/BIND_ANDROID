package com.teambind.bind_android.util.extension

import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.teambind.bind_android.R

fun ImageView.loadImage(
    url: String?,
    @DrawableRes placeholder: Int = R.drawable.ic_launcher_foreground,
    @DrawableRes error: Int = R.drawable.ic_launcher_foreground
) {
    Glide.with(context)
        .load(url)
        .placeholder(placeholder)
        .error(error)
        .into(this)
}

fun ImageView.loadCircleImage(
    url: String?,
    @DrawableRes placeholder: Int = R.drawable.ic_launcher_foreground,
    @DrawableRes error: Int = R.drawable.ic_launcher_foreground
) {
    Glide.with(context)
        .load(url)
        .apply(RequestOptions().transform(CircleCrop()))
        .placeholder(placeholder)
        .error(error)
        .into(this)
}

fun ImageView.loadRoundedImage(
    url: String?,
    radiusDp: Int = 8,
    @DrawableRes placeholder: Int = R.drawable.ic_launcher_foreground,
    @DrawableRes error: Int = R.drawable.ic_launcher_foreground
) {
    val radiusPx = context.dpToPx(radiusDp)
    Glide.with(context)
        .load(url)
        .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(radiusPx)))
        .placeholder(placeholder)
        .error(error)
        .into(this)
}

fun ImageView.loadImageWithCenterCrop(
    url: String?,
    @DrawableRes placeholder: Int = R.drawable.ic_launcher_foreground,
    @DrawableRes error: Int = R.drawable.ic_launcher_foreground
) {
    Glide.with(context)
        .load(url)
        .centerCrop()
        .placeholder(placeholder)
        .error(error)
        .into(this)
}

fun ImageView.clear() {
    Glide.with(context).clear(this)
}
