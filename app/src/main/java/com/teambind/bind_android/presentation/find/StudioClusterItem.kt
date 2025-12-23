package com.teambind.bind_android.presentation.find

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.teambind.bind_android.data.model.response.StudioDto

/**
 * 클러스터링을 위한 스튜디오 마커 아이템
 */
data class StudioClusterItem(
    val studio: StudioDto,
    private val position: LatLng,
    private val title: String,
    private val snippet: String
) : ClusterItem {

    override fun getPosition(): LatLng = position
    override fun getTitle(): String = title
    override fun getSnippet(): String = snippet
    override fun getZIndex(): Float = 0f

    companion object {
        fun from(studio: StudioDto): StudioClusterItem? {
            val lat = studio.latitude ?: return null
            val lng = studio.longitude ?: return null

            return StudioClusterItem(
                studio = studio,
                position = LatLng(lat, lng),
                title = studio.name ?: "스튜디오",
                snippet = studio.address ?: ""
            )
        }
    }
}
