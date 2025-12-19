package com.teambind.bind_android.presentation.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.teambind.bind_android.databinding.ItemBannerBinding

class BannerAdapter : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    // 배너 개수
    private val bannerCount = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val binding = ItemBannerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BannerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        // 레이아웃이 이미 완성되어 있어서 별도 바인딩 불필요
    }

    override fun getItemCount(): Int = bannerCount

    class BannerViewHolder(
        binding: ItemBannerBinding
    ) : RecyclerView.ViewHolder(binding.root)
}
