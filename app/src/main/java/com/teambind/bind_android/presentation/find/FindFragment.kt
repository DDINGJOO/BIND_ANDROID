package com.teambind.bind_android.presentation.find

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.chip.Chip
import com.google.maps.android.clustering.ClusterManager
import com.teambind.bind_android.R
import com.teambind.bind_android.databinding.FragmentFindBinding
import com.teambind.bind_android.presentation.base.BaseFragment
import com.teambind.bind_android.presentation.home.adapter.StudioAdapter
import com.teambind.bind_android.presentation.studiodetail.StudioDetailActivity
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint
import android.content.Intent
import android.view.View
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@AndroidEntryPoint
class FindFragment : BaseFragment<FragmentFindBinding>(), OnMapReadyCallback {

    private val viewModel: FindViewModel by viewModels()
    private var selectedFilterChip: Chip? = null

    private var googleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // 클러스터 매니저
    private var clusterManager: ClusterManager<StudioClusterItem>? = null

    // 스튜디오 목록 어댑터
    private val studioAdapter by lazy {
        StudioAdapter { studio ->
            navigateToStudioDetail(studio.studioId)
        }
    }

    // 기본 위치 (서울 강남역) - 위치를 못 가져올 경우 fallback
    private val defaultLocation = LatLng(37.4979, 127.0276)
    private val defaultZoom = 15f

    // 마지막 API 호출 위치 (iOS의 distinctUntilChanged 구현)
    private var lastApiCallLatitude = 0.0
    private var lastApiCallLongitude = 0.0
    private val minLocationChangeThreshold = 0.001 // 0.001도 이상 움직여야 API 호출

    // 초기 위치 설정 여부 (iOS의 isInitialLocationSet)
    private var isInitialLocationSet = false

    // 위치 권한 요청 런처
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            enableMyLocation()
            // 초기 위치가 아직 설정 안됐으면 초기 위치로 이동
            if (!isInitialLocationSet) {
                moveToInitialLocation()
            }
        } else {
            showToast("위치 권한이 필요합니다")
            // 권한 거부 시 기본 위치로 API 호출
            if (!isInitialLocationSet) {
                isInitialLocationSet = true
                lastApiCallLatitude = defaultLocation.latitude
                lastApiCallLongitude = defaultLocation.longitude
                viewModel.onMapCameraIdle(defaultLocation.latitude, defaultLocation.longitude)
                reverseGeocode(defaultLocation)
            }
        }
    }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentFindBinding {
        return FragmentFindBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        setupGoogleMap()
        setupRecyclerView()
        setupFilterChips()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        binding.rvStudios.adapter = studioAdapter
    }

    private fun setupGoogleMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapContainer) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // 지도 UI 설정
        map.uiSettings.apply {
            isZoomControlsEnabled = false
            isMyLocationButtonEnabled = false
            isCompassEnabled = false
            isMapToolbarEnabled = false
            isZoomGesturesEnabled = true      // 핀치 줌 활성화
            isScrollGesturesEnabled = true    // 스크롤 제스처 활성화
            isRotateGesturesEnabled = false   // 회전은 비활성화
            isTiltGesturesEnabled = false     // 틸트는 비활성화
        }

        // 커스텀 맵 스타일 적용 (POI 숨기기)
        try {
            map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
            )
        } catch (e: Exception) {
            // 스타일 적용 실패 시 기본 스타일 사용
        }

        // 클러스터 매니저 초기화
        setupClusterManager(map)

        // 위치 권한이 있으면 내 위치 표시 활성화 및 현재 위치로 이동
        if (hasLocationPermission()) {
            enableMyLocation()
            moveToInitialLocation()
        } else {
            // 권한이 없으면 기본 위치로 이동 후 권한 요청
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, defaultZoom))
            requestLocationPermission()
        }

        // 지도 클릭 리스너
        map.setOnMapClickListener { latLng ->
            // 지도 클릭 시 처리 (마커 선택 해제 등)
        }

        // 카메라 이동 완료 리스너
        map.setOnCameraIdleListener {
            // 클러스터 매니저에 카메라 이동 알림
            clusterManager?.onCameraIdle()

            val center = map.cameraPosition.target

            // iOS처럼 위치가 일정 이상 변경되었을 때만 API 호출 (distinctUntilChanged)
            val latDiff = kotlin.math.abs(center.latitude - lastApiCallLatitude)
            val lonDiff = kotlin.math.abs(center.longitude - lastApiCallLongitude)

            if (latDiff >= minLocationChangeThreshold || lonDiff >= minLocationChangeThreshold) {
                lastApiCallLatitude = center.latitude
                lastApiCallLongitude = center.longitude
                viewModel.onMapCameraIdle(center.latitude, center.longitude)
            }

            // 역지오코딩으로 현재 위치 주소 업데이트
            reverseGeocode(center)
        }
    }

    // 클러스터 매니저 초기화
    private fun setupClusterManager(map: GoogleMap) {
        clusterManager = ClusterManager<StudioClusterItem>(requireContext(), map).apply {
            // 클러스터 아이템(개별 마커) 클릭 리스너
            setOnClusterItemClickListener { item ->
                viewModel.selectStudio(item.studio)
                showStudioPreviewBottomSheet(item.studio)
                true
            }

            // 클러스터(그룹) 클릭 리스너
            setOnClusterClickListener { cluster ->
                // 클러스터 클릭 시 확대
                val builder = com.google.android.gms.maps.model.LatLngBounds.Builder()
                cluster.items.forEach { builder.include(it.position) }
                val bounds = builder.build()
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                true
            }
        }

        // 마커 클릭을 클러스터 매니저가 처리하도록 설정
        map.setOnMarkerClickListener(clusterManager)
    }

    // 위치 권한 체크
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    // 위치 권한 요청
    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // 내 위치 표시 활성화
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (hasLocationPermission()) {
            googleMap?.isMyLocationEnabled = true
        }
    }

    // 초기 위치로 이동 (앱 시작 시 한 번만 호출)
    @SuppressLint("MissingPermission")
    private fun moveToInitialLocation() {
        if (!hasLocationPermission() || isInitialLocationSet) {
            return
        }

        val cancellationToken = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationToken.token
        ).addOnSuccessListener { location ->
            if (!isInitialLocationSet) {
                isInitialLocationSet = true
                val targetLocation = location?.let {
                    LatLng(it.latitude, it.longitude)
                } ?: defaultLocation

                googleMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(targetLocation, defaultZoom)
                )

                // 초기 위치 설정 후 즉시 API 호출
                lastApiCallLatitude = targetLocation.latitude
                lastApiCallLongitude = targetLocation.longitude
                viewModel.onMapCameraIdle(targetLocation.latitude, targetLocation.longitude)
                reverseGeocode(targetLocation)
            }
        }.addOnFailureListener {
            if (!isInitialLocationSet) {
                isInitialLocationSet = true
                // 위치 가져오기 실패 시 기본 위치 사용
                googleMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(defaultLocation, defaultZoom)
                )
                lastApiCallLatitude = defaultLocation.latitude
                lastApiCallLongitude = defaultLocation.longitude
                viewModel.onMapCameraIdle(defaultLocation.latitude, defaultLocation.longitude)
                reverseGeocode(defaultLocation)
            }
        }
    }

    // 현재 위치로 이동 (FAB 버튼 클릭 시)
    @SuppressLint("MissingPermission")
    private fun moveToCurrentLocation() {
        if (!hasLocationPermission()) {
            requestLocationPermission()
            return
        }

        val cancellationToken = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationToken.token
        ).addOnSuccessListener { location ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                googleMap?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(currentLatLng, defaultZoom)
                )
            } ?: run {
                showToast("현재 위치를 가져올 수 없습니다")
            }
        }.addOnFailureListener {
            showToast("위치 가져오기 실패: ${it.message}")
        }
    }

    // 역지오코딩: 좌표 → 주소 변환
    private fun reverseGeocode(latLng: LatLng) {
        lifecycleScope.launch {
            val address = withContext(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(requireContext(), Locale.KOREA)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        var result: String? = null
                        geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) { addresses ->
                            result = addresses.firstOrNull()?.let { addr ->
                                formatAddress(addr.adminArea, addr.locality, addr.subLocality, addr.thoroughfare)
                            }
                        }
                        // API 33+ 콜백이 비동기라 잠시 대기
                        Thread.sleep(100)
                        result
                    } else {
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                        addresses?.firstOrNull()?.let { addr ->
                            formatAddress(addr.adminArea, addr.locality, addr.subLocality, addr.thoroughfare)
                        }
                    }
                } catch (e: Exception) {
                    null
                }
            }
            address?.let {
                viewModel.updateAddress(it)
            }
        }
    }

    // 주소 포맷팅 (시도 + 시군구 + 동/읍면)
    private fun formatAddress(
        adminArea: String?,      // 시/도
        locality: String?,       // 시/군/구
        subLocality: String?,    // 동/읍/면
        thoroughfare: String?    // 도로명
    ): String {
        val parts = mutableListOf<String>()
        adminArea?.let { parts.add(it) }
        locality?.let { if (it != adminArea) parts.add(it) }
        subLocality?.let { parts.add(it) }
        return if (parts.isNotEmpty()) parts.joinToString(" ") else "위치 정보 없음"
    }

    override fun initObserver() {
        collectLatestFlow(viewModel.uiState) { state ->
            // 현재 주소 업데이트
            binding.tvLocation.text = state.currentAddress

            // 스튜디오 목록 업데이트
            if (state.studios.isNotEmpty()) {
                updateMapMarkers(state.studios)
                studioAdapter.submitList(state.studios)
            }

            // 뷰 모드에 따라 UI 전환
            updateViewMode(state.viewMode)

            // 에러 메시지 표시
            state.errorMessage?.let {
                showToast(it)
                viewModel.clearError()
            }
        }
    }

    private fun updateViewMode(viewMode: FindViewMode) {
        when (viewMode) {
            FindViewMode.MAP -> {
                binding.mapContainer.visibility = View.VISIBLE
                binding.rvStudios.visibility = View.GONE
                binding.fabMyLocation.visibility = View.VISIBLE
                binding.ivToggleIcon.setImageResource(R.drawable.ic_list)
                binding.tvToggleText.text = "목록보기"
            }
            FindViewMode.LIST -> {
                binding.mapContainer.visibility = View.GONE
                binding.rvStudios.visibility = View.VISIBLE
                binding.fabMyLocation.visibility = View.GONE
                binding.ivToggleIcon.setImageResource(R.drawable.ic_map)
                binding.tvToggleText.text = "지도보기"
            }
        }
    }

    private fun navigateToStudioDetail(studioId: String?) {
        studioId?.let { id ->
            val intent = Intent(requireContext(), StudioDetailActivity::class.java).apply {
                putExtra("placeId", id)
            }
            startActivity(intent)
        }
    }

    private fun navigateToSearch() {
        val intent = Intent(requireContext(), com.teambind.bind_android.presentation.search.SearchActivity::class.java)
        startActivity(intent)
    }

    // Bottom Sheet 표시
    private fun showStudioPreviewBottomSheet(studio: com.teambind.bind_android.data.model.response.StudioDto) {
        StudioPreviewBottomSheet.newInstance(studio)
            .show(childFragmentManager, StudioPreviewBottomSheet.TAG)
    }

    private fun updateMapMarkers(studios: List<com.teambind.bind_android.data.model.response.StudioDto>) {
        clusterManager?.let { manager ->
            // 기존 클러스터 아이템 제거
            manager.clearItems()

            // 새 클러스터 아이템 추가
            studios.forEach { studio ->
                StudioClusterItem.from(studio)?.let { item ->
                    manager.addItem(item)
                }
            }

            // 클러스터 다시 계산
            manager.cluster()
        }
    }

    private fun setupFilterChips() {
        val chips = listOf(
            binding.chipRegion,
            binding.chipSpace,
            binding.chipDateTime,
            binding.chipPersonnel,
            binding.chipKeyword
        )

        chips.forEach { chip ->
            chip.setOnClickListener {
                // Toggle chip selection
                if (selectedFilterChip == chip) {
                    chip.isChecked = false
                    selectedFilterChip = null
                } else {
                    selectedFilterChip?.isChecked = false
                    chip.isChecked = true
                    selectedFilterChip = chip
                    onFilterSelected(chip.text.toString())
                }
            }
        }
    }

    private fun onFilterSelected(filter: String) {
        // TODO: Show filter bottom sheet based on filter type
        showToast("$filter 필터 선택")
    }

    private fun setupClickListeners() {
        with(binding) {
            // Location selector
            layoutLocation.setOnSingleClickListener {
                // TODO: Show location selector
                showToast("위치 선택")
            }

            // Search bar
            searchBar.setOnSingleClickListener {
                navigateToSearch()
            }

            // Filter button
            btnFilter.setOnSingleClickListener {
                // TODO: Show filter options
                showToast("필터 옵션")
            }

            // Show list/map toggle button
            btnShowList.setOnSingleClickListener {
                viewModel.toggleViewMode()
            }

            // Current location button
            fabMyLocation.setOnSingleClickListener {
                moveToCurrentLocation()
            }
        }
    }
}
