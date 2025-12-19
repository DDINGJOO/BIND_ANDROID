package com.teambind.bind_android.presentation.studiodetail

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayout
import com.teambind.bind_android.R
import com.google.gson.Gson
import com.teambind.bind_android.data.model.response.PlaceDetailDto
import com.teambind.bind_android.data.model.response.PricingPolicyDto
import com.teambind.bind_android.data.model.response.RoomDetailDto
import com.teambind.bind_android.data.model.response.RoomDetailResponse
import com.teambind.bind_android.databinding.ActivityStudioDetailBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.presentation.selecttime.SelectTimeActivity
import com.teambind.bind_android.presentation.studiodetail.adapter.CalendarDay
import com.teambind.bind_android.presentation.studiodetail.adapter.CalendarDayAdapter
import com.teambind.bind_android.presentation.studiodetail.adapter.ImagePagerAdapter
import com.teambind.bind_android.presentation.studiodetail.adapter.RoomAdapter
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@AndroidEntryPoint
class StudioDetailActivity : BaseActivity<ActivityStudioDetailBinding>() {

    private val viewModel: StudioDetailViewModel by viewModels()

    private var indicatorDots: MutableList<ImageView> = mutableListOf()
    private val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)

    private var isDescriptionExpanded = false
    private var currentTabIndex = 0
    private var roomCount = 0

    // 페이지 타입: true = Room Detail, false = Place Detail
    private var isRoomDetail = false
    private var currentRoomId: Long = 0L
    private var currentPlaceId: String = ""

    // 달력 관련
    private var currentYearMonth = YearMonth.now()
    private var selectedDate: LocalDate? = null
    private val calendarAdapter by lazy {
        CalendarDayAdapter { date ->
            onDateSelected(date)
        }
    }

    // 예약 완료 후 결과 처리
    private val selectTimeLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    // Room 목록 어댑터 (Place Detail용) - RoomDetailResponse 사용
    private val roomAdapter by lazy {
        RoomAdapter { roomDetail ->
            // 이미 받은 데이터로 Room Detail 화면 열기
            roomDetail.room?.roomId?.let { roomId ->
                StudioDetailActivity.startWithRoomDetail(this, roomDetail)
            }
        }
    }

    // 다른 룸 목록 어댑터 (Room Detail용) - RoomDetailResponse 사용
    private val otherRoomsAdapter by lazy {
        RoomAdapter { roomDetail ->
            // 이미 받은 데이터로 Room Detail 화면 열기
            roomDetail.room?.roomId?.let { roomId ->
                StudioDetailActivity.startWithRoomDetail(this, roomDetail)
            }
        }
    }

    override fun inflateBinding(): ActivityStudioDetailBinding {
        return ActivityStudioDetailBinding.inflate(layoutInflater)
    }

    override fun initView() {
        currentRoomId = intent.getLongExtra(EXTRA_ROOM_ID, 0)
        currentPlaceId = intent.getStringExtra(EXTRA_PLACE_ID) ?: ""
        val roomDetailJson = intent.getStringExtra(EXTRA_ROOM_DETAIL_JSON)

        isRoomDetail = currentRoomId > 0

        when {
            // 이미 받은 RoomDetailResponse 데이터가 있으면 사용 (재조회 불필요)
            !roomDetailJson.isNullOrEmpty() -> {
                try {
                    val roomDetail = Gson().fromJson(roomDetailJson, RoomDetailResponse::class.java)
                    viewModel.setRoomDetailDirectly(roomDetail)
                } catch (e: Exception) {
                    // JSON 파싱 실패 시 API로 재조회
                    viewModel.loadRoomDetail(currentRoomId)
                }
            }
            currentRoomId > 0 -> viewModel.loadRoomDetail(currentRoomId)
            currentPlaceId.isNotEmpty() -> viewModel.loadPlaceDetail(currentPlaceId)
            else -> {
                showToast("공간 정보를 찾을 수 없습니다.")
                finish()
                return
            }
        }

        setupToolbar()
        setupTabLayout()
        setupClickListeners()
        setupRoomList()
        setupCalendar()
    }

    private fun setupRoomList() {
        binding.rvRooms.adapter = roomAdapter
        binding.rvOtherRooms.adapter = otherRoomsAdapter
    }

    private fun setupCalendar() {
        binding.rvCalendar.adapter = calendarAdapter
        updateCalendar()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnSingleClickListener {
            finish()
        }
    }

    private fun setupTabLayout() {
        with(binding.tabLayout) {
            removeAllTabs()
            addTab(newTab().setText("정보"))
            // Room Detail: "예약하기", Place Detail: "룸 정보"
            addTab(newTab().setText(if (isRoomDetail) "예약하기" else "룸 정보"))

            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    currentTabIndex = tab?.position ?: 0
                    updateSectionVisibility()
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }
    }

    private fun updateSectionVisibility() {
        with(binding) {
            if (isRoomDetail) {
                // Room Detail 모드
                when (currentTabIndex) {
                    0 -> {
                        // 정보 탭: Info + 업체정보 + 상세정보 + 다른 룸
                        layoutInfoSection.visibility = View.VISIBLE
                        layoutStoreInfoSection.visibility = View.VISIBLE
                        layoutDescriptionSection.visibility = View.VISIBLE
                        layoutOtherRoomsSection.visibility = View.VISIBLE
                        layoutReservationSection.visibility = View.GONE
                        layoutRoomSection.visibility = View.GONE
                    }
                    1 -> {
                        // 예약하기 탭: 달력
                        layoutInfoSection.visibility = View.GONE
                        layoutStoreInfoSection.visibility = View.GONE
                        layoutDescriptionSection.visibility = View.GONE
                        layoutOtherRoomsSection.visibility = View.GONE
                        layoutReservationSection.visibility = View.VISIBLE
                        layoutRoomSection.visibility = View.GONE
                    }
                }
            } else {
                // Place Detail 모드
                when (currentTabIndex) {
                    0 -> {
                        // 정보 탭
                        layoutInfoSection.visibility = View.VISIBLE
                        layoutRoomSection.visibility = View.GONE
                        layoutReservationSection.visibility = View.GONE
                        layoutStoreInfoSection.visibility = View.GONE
                        layoutDescriptionSection.visibility = View.GONE
                        layoutOtherRoomsSection.visibility = View.GONE
                    }
                    1 -> {
                        // 룸 정보 탭
                        layoutInfoSection.visibility = View.GONE
                        layoutRoomSection.visibility = View.VISIBLE
                        updateRoomSectionHeader()
                        layoutReservationSection.visibility = View.GONE
                        layoutStoreInfoSection.visibility = View.GONE
                        layoutDescriptionSection.visibility = View.GONE
                        layoutOtherRoomsSection.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun updateRoomSectionHeader() {
        binding.tvRoomHeader.text = if (roomCount > 0) {
            "룸 정보 $roomCount"
        } else {
            "룸 정보"
        }
    }

    private fun setupClickListeners() {
        with(binding) {
            ivBookmark.setOnSingleClickListener {
                viewModel.toggleBookmark()
            }

            // 설명 더보기 클릭
            tvDescriptionMore.setOnSingleClickListener {
                toggleDescription()
            }

            tvDescription.setOnSingleClickListener {
                if (tvDescriptionMore.visibility == View.VISIBLE) {
                    toggleDescription()
                }
            }

            // 전화번호 복사
            btnCopyPhone.setOnSingleClickListener {
                val phone = tvInfoPhone.text.toString()
                if (phone.isNotEmpty()) {
                    copyToClipboard(phone)
                    showToast("전화번호가 복사되었습니다.")
                }
            }

            // 이용정책 확인
            layoutPolicy.setOnSingleClickListener {
                showPolicyBottomSheet()
            }

            // 업체정보 카드 클릭 -> Place Detail로 이동
            cardStoreInfo.setOnSingleClickListener {
                val placeId = viewModel.uiState.value.place?.id
                if (!placeId.isNullOrEmpty()) {
                    StudioDetailActivity.startWithPlace(this@StudioDetailActivity, placeId)
                }
            }

            // 가격 상세보기 클릭
            btnPriceDetail.setOnSingleClickListener {
                showPriceDetailDialog()
            }

            // 달력 이전/다음 월 버튼
            btnPrevMonth.setOnSingleClickListener {
                currentYearMonth = currentYearMonth.minusMonths(1)
                updateCalendar()
            }

            btnNextMonth.setOnSingleClickListener {
                currentYearMonth = currentYearMonth.plusMonths(1)
                updateCalendar()
            }
        }
    }

    // 달력 날짜 선택
    private fun onDateSelected(date: LocalDate) {
        selectedDate = date

        // 선택된 날짜 표시 업데이트
        val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
        binding.tvSelectedDate.text = "${date.monthValue}월 ${date.dayOfMonth}일 ($dayOfWeek)"
        binding.tvSelectedDate.setTextColor(ContextCompat.getColor(this, R.color.black))

        // 달력 UI 업데이트
        updateCalendar()

        // 시간 선택 화면으로 이동
        val currentState = viewModel.uiState.value
        val room = currentState.room
        val place = currentState.place
        val pricingPolicy = currentState.pricingPolicy

        if (room != null) {
            navigateToSelectTime(
                roomId = room.roomId,
                placeId = place?.id?.toLongOrNull() ?: room.placeId,
                roomName = room.roomName,
                roomImageUrl = room.imageUrls?.firstOrNull(),
                selectedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                minUnit = getMinUnitFromTimeSlot(pricingPolicy?.timeSlot)
            )
        }
    }

    // 달력 UI 업데이트
    private fun updateCalendar() {
        binding.tvCalendarMonth.text = "${currentYearMonth.year}년 ${currentYearMonth.monthValue}월"

        val days = mutableListOf<CalendarDay>()
        val firstDayOfMonth = currentYearMonth.atDay(1)
        val lastDayOfMonth = currentYearMonth.atEndOfMonth()
        val today = LocalDate.now()

        // 첫 주의 빈 칸 (일요일 = 0)
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
        for (i in 0 until firstDayOfWeek) {
            days.add(CalendarDay(date = null))
        }

        // 해당 월의 날짜들
        var currentDate = firstDayOfMonth
        while (!currentDate.isAfter(lastDayOfMonth)) {
            days.add(
                CalendarDay(
                    date = currentDate,
                    isCurrentMonth = true,
                    isToday = currentDate == today,
                    isSelected = currentDate == selectedDate,
                    isPastDate = currentDate.isBefore(today)
                )
            )
            currentDate = currentDate.plusDays(1)
        }

        calendarAdapter.submitList(days)
    }

    private fun showPolicyBottomSheet() {
        PolicyGuideBottomSheet.newInstance()
            .show(supportFragmentManager, "PolicyGuide")
    }

    private fun showPriceDetailDialog() {
        val pricingPolicy = viewModel.uiState.value.pricingPolicy ?: return
        PriceDetailBottomSheet.newInstance(pricingPolicy)
            .show(supportFragmentManager, "PriceDetailBottomSheet")
    }

    private fun toggleDescription() {
        isDescriptionExpanded = !isDescriptionExpanded
        with(binding) {
            if (isDescriptionExpanded) {
                tvDescription.maxLines = Int.MAX_VALUE
                tvDescriptionMore.text = "접기"
            } else {
                tvDescription.maxLines = 2
                tvDescriptionMore.text = "더보기"
            }
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("phone", text)
        clipboard.setPrimaryClip(clip)
    }

    override fun initObserver() {
        collectLatestFlow(viewModel.uiState) { state ->
            when {
                state.room != null && state.place != null -> {
                    updateRoomInfo(state.room, state.place, state.pricingPolicy)
                }
                state.place != null -> {
                    updatePlaceOnlyInfo(state.place, state.roomLoadFailed)
                }
            }

            // Room 목록 업데이트 (RoomDetailResponse 리스트)
            updateRoomList(state.roomDetails)

            // 북마크 상태
            binding.ivBookmark.setImageResource(
                if (state.isBookmarked) R.drawable.ic_bookmark_filled
                else R.drawable.ic_bookmark_empty
            )

            // 에러 메시지
            state.errorMessage?.let {
                showToast(it)
                viewModel.clearError()
            }

            if (state.loadFailed) {
                finish()
            }
        }
    }

    private fun updateRoomList(roomDetails: List<RoomDetailResponse>) {
        with(binding) {
            if (roomDetails.isNotEmpty()) {
                if (isRoomDetail) {
                    // Room Detail: 다른 룸 목록 (현재 Room 제외)
                    val otherRooms = roomDetails.filter { it.room?.roomId != currentRoomId }
                    if (otherRooms.isNotEmpty()) {
                        otherRoomsAdapter.submitList(otherRooms)
                        rvOtherRooms.visibility = View.VISIBLE
                    } else {
                        layoutOtherRoomsSection.visibility = View.GONE
                    }
                } else {
                    // Place Detail: 룸 목록
                    roomAdapter.submitList(roomDetails)
                    rvRooms.visibility = View.VISIBLE
                    layoutRoomEmpty.visibility = View.GONE
                    roomCount = roomDetails.size
                    updateRoomSectionHeader()
                }
            }
        }
    }

    private fun updatePlaceOnlyInfo(place: PlaceDetailDto, roomLoadFailed: Boolean) {
        with(binding) {
            val images = place.imageUrls
            if (!images.isNullOrEmpty()) {
                setupImagePager(images)
            }

            tvName.text = place.placeName ?: ""
            tvDistance.visibility = View.GONE
            tvDot.visibility = View.GONE

            val shortAddress = place.location?.address?.shortAddress
                ?: place.location?.address?.fullAddress
            if (!shortAddress.isNullOrEmpty()) {
                tvAddress.text = shortAddress
                tvAddress.visibility = View.VISIBLE
            } else {
                tvAddress.visibility = View.GONE
            }

            val description = place.description
            if (!description.isNullOrEmpty()) {
                tvDescription.text = description
                tvDescription.visibility = View.VISIBLE
                tvDescription.post {
                    tvDescriptionMore.visibility = if (tvDescription.lineCount > 2) View.VISIBLE else View.GONE
                }
            } else {
                tvDescription.visibility = View.GONE
                tvDescriptionMore.visibility = View.GONE
            }

            val keywords = place.keywords
            if (!keywords.isNullOrEmpty()) {
                chipGroupKeywords.visibility = View.VISIBLE
                chipGroupKeywords.removeAllViews()
                keywords.forEach { keyword ->
                    val chip = Chip(this@StudioDetailActivity).apply {
                        text = "#$keyword"
                        isClickable = false
                        setChipBackgroundColorResource(R.color.background)
                        setTextColor(ContextCompat.getColor(context, R.color.gray6))
                        chipStrokeWidth = 0f
                        textSize = 13f
                    }
                    chipGroupKeywords.addView(chip)
                }
            } else {
                chipGroupKeywords.visibility = View.GONE
            }

            bindInfoSection(place, null, null)
            roomCount = place.roomCount ?: place.roomIds?.size ?: 0
            updateRoomEmptyState(roomLoadFailed, roomCount)

            // Place Detail: Room 전용 섹션 숨기기
            rowOperatingHours.visibility = View.GONE
            rowPrice.visibility = View.GONE
            rowCapacity.visibility = View.GONE
        }
    }

    private fun updateRoomInfo(room: RoomDetailDto, place: PlaceDetailDto, pricingPolicy: PricingPolicyDto?) {
        with(binding) {
            val images = room.imageUrls ?: place.imageUrls
            if (!images.isNullOrEmpty()) {
                setupImagePager(images)
            }

            // 카테고리 · 업체명 표시 (iOS처럼)
            val category = place.category
            val placeName = place.placeName
            if (!category.isNullOrEmpty() && !placeName.isNullOrEmpty()) {
                tvAddress.text = "$category · $placeName"
                tvAddress.visibility = View.VISIBLE
            }

            tvName.text = room.roomName ?: place.placeName ?: ""
            tvDistance.visibility = View.GONE
            tvDot.visibility = View.GONE

            val description = place.description
            if (!description.isNullOrEmpty()) {
                tvDescription.text = description
                tvDescription.visibility = View.VISIBLE
                tvDescription.post {
                    tvDescriptionMore.visibility = if (tvDescription.lineCount > 2) View.VISIBLE else View.GONE
                }
            } else {
                tvDescription.visibility = View.GONE
                tvDescriptionMore.visibility = View.GONE
            }

            val keywords = place.keywords
            if (!keywords.isNullOrEmpty()) {
                chipGroupKeywords.visibility = View.VISIBLE
                chipGroupKeywords.removeAllViews()
                keywords.forEach { keyword ->
                    val chip = Chip(this@StudioDetailActivity).apply {
                        text = "#$keyword"
                        isClickable = false
                        setChipBackgroundColorResource(R.color.background)
                        setTextColor(ContextCompat.getColor(context, R.color.gray6))
                        chipStrokeWidth = 0f
                        textSize = 13f
                    }
                    chipGroupKeywords.addView(chip)
                }
            } else {
                chipGroupKeywords.visibility = View.GONE
            }

            bindInfoSection(place, room, pricingPolicy)
            bindStoreInfoSection(place)
            bindDescriptionSection(room)
            bindOtherRoomsSection(place)

            roomCount = place.roomCount ?: place.roomIds?.size ?: 0
            updateRoomEmptyState(false, roomCount)
        }
    }

    private fun bindInfoSection(place: PlaceDetailDto, room: RoomDetailDto?, pricingPolicy: PricingPolicyDto?) {
        with(binding) {
            // Room Detail 전용 항목들
            if (isRoomDetail && pricingPolicy != null) {
                // 영업시간 (TODO: API에서 가져오기)
                rowOperatingHours.visibility = View.VISIBLE
                tvInfoOperatingHours.text = "운영시간 정보 없음"

                // 이용요금
                val priceText = formatPriceInfo(pricingPolicy)
                if (priceText.isNotEmpty()) {
                    rowPrice.visibility = View.VISIBLE
                    tvInfoPrice.text = priceText
                } else {
                    rowPrice.visibility = View.GONE
                }

                // 수용인원
                val maxOccupancy = room?.maxOccupancy
                if (maxOccupancy != null && maxOccupancy > 0) {
                    rowCapacity.visibility = View.VISIBLE
                    tvInfoCapacity.text = "최대 ${maxOccupancy}명"
                } else {
                    rowCapacity.visibility = View.VISIBLE
                    tvInfoCapacity.text = "10+"
                }
            }

            // 주소
            val fullAddress = buildString {
                place.location?.address?.fullAddress?.let { append(it) }
                place.location?.address?.addressDetail?.let { detail ->
                    if (detail.isNotEmpty()) append(" $detail")
                }
            }
            if (fullAddress.isNotEmpty()) {
                tvInfoAddress.text = fullAddress
                rowAddress.visibility = View.VISIBLE
            } else {
                rowAddress.visibility = View.GONE
            }

            // 오는 길
            val locationGuide = place.location?.locationGuide
            if (!locationGuide.isNullOrEmpty()) {
                rowDirections.visibility = View.VISIBLE
                tvInfoDirections.text = locationGuide
            } else {
                rowDirections.visibility = View.GONE
            }

            // 전화번호
            val phone = place.contact?.contact
            if (!phone.isNullOrEmpty()) {
                rowPhone.visibility = View.VISIBLE
                tvInfoPhone.text = phone
            } else {
                rowPhone.visibility = View.GONE
            }

            // 주차정보
            val parking = place.parking
            if (parking != null) {
                val parkingText = if (parking.available == true) {
                    val typeText = when (parking.parkingType) {
                        "FREE" -> "무료"
                        "PAID" -> "유료"
                        else -> ""
                    }
                    if (typeText.isNotEmpty()) "주차가능 · $typeText" else "주차가능"
                } else {
                    "주차불가"
                }
                tvInfoParking.text = parkingText
                rowParking.visibility = View.VISIBLE
            } else {
                rowParking.visibility = View.GONE
            }

            // 추가정보 (웹사이트)
            val website = place.contact?.websites?.firstOrNull()
            if (!website.isNullOrEmpty()) {
                rowAdditionalInfo.visibility = View.VISIBLE
                tvInfoAdditional.text = website
            } else {
                rowAdditionalInfo.visibility = View.GONE
            }
        }
    }

    private fun formatPriceInfo(pricingPolicy: PricingPolicyDto): String {
        val defaultPrice = pricingPolicy.defaultPrice ?: return ""
        // 시간당 가격으로 환산 (HALF_HOUR인 경우 2배)
        val hourlyPrice = when (pricingPolicy.timeSlot) {
            "HALF_HOUR" -> defaultPrice * 2
            else -> defaultPrice
        }
        return "${numberFormat.format(hourlyPrice)}원/시간"
    }

    private fun bindStoreInfoSection(place: PlaceDetailDto) {
        with(binding) {
            // 업체 썸네일
            val storeImage = place.imageUrls?.firstOrNull()
            if (!storeImage.isNullOrEmpty()) {
                Glide.with(this@StudioDetailActivity)
                    .load(storeImage)
                    .placeholder(R.drawable.bg_rounded_rect)
                    .centerCrop()
                    .into(ivStoreThumb)
            }

            // 업체명
            tvStoreName.text = place.placeName ?: ""

            // 공간 개수
            val roomCountValue = place.roomCount ?: place.roomIds?.size ?: 0
            tvStoreRoomCount.text = "공간 ${roomCountValue}개"

            layoutStoreInfoSection.visibility = View.VISIBLE
        }
    }

    private fun bindDescriptionSection(room: RoomDetailDto) {
        with(binding) {
            val furtherDetails = room.furtherDetails
            if (!furtherDetails.isNullOrEmpty()) {
                tvDetailDescription.text = furtherDetails.joinToString("\n") { "• $it" }
                layoutDescriptionSection.visibility = View.VISIBLE
            } else {
                layoutDescriptionSection.visibility = View.GONE
            }
        }
    }

    private fun bindOtherRoomsSection(place: PlaceDetailDto) {
        with(binding) {
            val placeName = place.placeName ?: "업체"
            tvOtherRoomsHeader.text = "${placeName}의 다른 룸"
        }
    }

    private fun updateRoomEmptyState(hasError: Boolean, count: Int) {
        with(binding) {
            val currentRooms = viewModel.uiState.value.roomDetails
            if (currentRooms.isNotEmpty()) {
                rvRooms.visibility = View.VISIBLE
                layoutRoomEmpty.visibility = View.GONE
            } else if (count == 0) {
                rvRooms.visibility = View.GONE
                layoutRoomEmpty.visibility = View.VISIBLE
                tvRoomEmptyMessage.text = if (hasError) {
                    "공간을 불러올 수 없습니다."
                } else {
                    "등록된 공간이 없습니다."
                }
            } else {
                rvRooms.visibility = View.GONE
                layoutRoomEmpty.visibility = View.VISIBLE
                tvRoomEmptyMessage.text = "룸 목록 로드 중..."
            }
        }
    }

    private fun setupImagePager(images: List<String>) {
        val adapter = ImagePagerAdapter(images)
        binding.vpImages.adapter = adapter

        setupIndicators(images.size)
        binding.vpImages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateIndicators(position)
            }
        })
    }

    private fun setupIndicators(count: Int) {
        binding.layoutIndicator.removeAllViews()
        indicatorDots.clear()

        if (count <= 1) return

        for (i in 0 until count) {
            val dot = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.indicator_dot_size),
                    resources.getDimensionPixelSize(R.dimen.indicator_dot_size)
                ).apply {
                    marginStart = if (i > 0) resources.getDimensionPixelSize(R.dimen.indicator_dot_margin) else 0
                }
                setImageResource(R.drawable.selector_indicator_dot)
                isSelected = i == 0
            }
            indicatorDots.add(dot)
            binding.layoutIndicator.addView(dot)
        }
    }

    private fun updateIndicators(position: Int) {
        indicatorDots.forEachIndexed { index, imageView ->
            imageView.isSelected = index == position
        }
    }

    private fun navigateToSelectTime(
        roomId: Long,
        placeId: Long,
        roomName: String,
        roomImageUrl: String?,
        selectedDate: String,
        minUnit: Int
    ) {
        SelectTimeActivity.startForResult(
            launcher = selectTimeLauncher,
            context = this,
            roomId = roomId,
            placeId = placeId,
            roomName = roomName,
            roomImageUrl = roomImageUrl,
            selectedDate = selectedDate,
            minUnit = minUnit
        )
    }

    private fun getMinUnitFromTimeSlot(timeSlot: String?): Int {
        return when (timeSlot) {
            "HOUR" -> 60
            "HALF_HOUR" -> 30
            "QUARTER_HOUR" -> 15
            else -> 60
        }
    }

    companion object {
        private const val EXTRA_ROOM_ID = "extra_room_id"
        private const val EXTRA_PLACE_ID = "extra_place_id"
        private const val EXTRA_ROOM_DETAIL_JSON = "extra_room_detail_json"

        fun start(context: Context, roomId: Long) {
            val intent = Intent(context, StudioDetailActivity::class.java).apply {
                putExtra(EXTRA_ROOM_ID, roomId)
            }
            context.startActivity(intent)
        }

        fun startWithPlace(context: Context, placeId: String) {
            val intent = Intent(context, StudioDetailActivity::class.java).apply {
                putExtra(EXTRA_PLACE_ID, placeId)
            }
            context.startActivity(intent)
        }

        // 이미 받은 RoomDetailResponse 데이터로 Room Detail 화면 열기
        fun startWithRoomDetail(context: Context, roomDetail: RoomDetailResponse) {
            val intent = Intent(context, StudioDetailActivity::class.java).apply {
                val roomId = roomDetail.room?.roomId ?: 0L
                putExtra(EXTRA_ROOM_ID, roomId)
                putExtra(EXTRA_ROOM_DETAIL_JSON, Gson().toJson(roomDetail))
            }
            context.startActivity(intent)
        }
    }
}
