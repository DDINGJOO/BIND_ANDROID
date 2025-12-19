package com.teambind.bind_android.presentation.start.profilesetting

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.teambind.bind_android.R
import com.teambind.bind_android.data.local.PreferencesManager
import com.teambind.bind_android.data.model.Genre
import com.teambind.bind_android.data.model.Instrument
import com.teambind.bind_android.databinding.ActivityProfileSettingBinding
import com.teambind.bind_android.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileSettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileSettingBinding
    private val viewModel: ProfileSettingViewModel by viewModels()

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private var settingType: ProfileSettingType = ProfileSettingType.SETTING

    // 이미지 선택 결과 처리
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { handleImageSelected(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingType = if (intent.getBooleanExtra(EXTRA_IS_EDIT_MODE, false)) {
            ProfileSettingType.EDIT
        } else {
            ProfileSettingType.SETTING
        }

        // ViewModel에 타입 전달
        viewModel.setSettingType(settingType)

        setupUI()
        setupListeners()
        observeViewModel()

        // EDIT 모드에서만 기존 프로필 정보 로드
        if (settingType == ProfileSettingType.EDIT) {
            viewModel.fetchMyProfile()
        }
    }

    private fun setupUI() {
        when (settingType) {
            ProfileSettingType.SETTING -> {
                binding.tvTitle.visibility = View.GONE
                binding.tvSubtitle.visibility = View.VISIBLE
                binding.btnNext.text = "약관동의하고 회원가입 하기"
                // 초기 설정 모드에서는 장르, 악기, 자기소개 숨김
                binding.layoutGenreSection.visibility = View.GONE
                binding.layoutInstrumentSection.visibility = View.GONE
                binding.layoutIntroductionSection.visibility = View.GONE
            }

            ProfileSettingType.EDIT -> {
                binding.tvTitle.visibility = View.VISIBLE
                binding.tvTitle.text = "프로필 수정"
                binding.tvSubtitle.visibility = View.GONE
                binding.btnNext.text = "수정완료"
                // 수정 모드에서는 장르, 악기, 자기소개 표시
                binding.layoutGenreSection.visibility = View.VISIBLE
                binding.layoutInstrumentSection.visibility = View.VISIBLE
                binding.layoutIntroductionSection.visibility = View.VISIBLE
                // 수정 모드에서는 버튼 항상 활성화
                binding.btnNext.isEnabled = true
                binding.btnNext.setBackgroundResource(R.drawable.bg_confirm_button)
            }
        }
    }

    private fun setupListeners() {
        // 뒤로가기
        binding.ivBack.setOnClickListener {
            finish()
        }

        // 프로필 이미지 선택
        binding.ivProfile.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        binding.ivEditPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // 닉네임 입력
        binding.etNickname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.setNickname(s?.toString() ?: "")
            }
        })

        // 닉네임 중복 확인
        binding.btnCheckNickname.setOnClickListener {
            viewModel.checkNicknameAvailable()
        }

        // 성별 선택
        binding.btnGenderMale.setOnClickListener {
            selectGender("M")
        }
        binding.btnGenderFemale.setOnClickListener {
            selectGender("F")
        }

        // 지역 선택
        binding.btnSelectRegion.setOnClickListener {
            showRegionSelector()
        }

        // 채팅 허용 토글
        binding.layoutChattable.setOnClickListener {
            viewModel.toggleChattable()
        }

        // 프로필 공개 토글
        binding.layoutPublicProfile.setOnClickListener {
            viewModel.togglePublicProfile()
        }

        // 장르 선택
        binding.btnSelectGenres.setOnClickListener {
            showGenreSelector()
        }

        // 악기 선택
        binding.btnSelectInstruments.setOnClickListener {
            showInstrumentSelector()
        }

        // 자기소개 입력
        binding.etIntroduction.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString() ?: ""
                viewModel.setIntroduction(text)
                binding.tvIntroductionCount.text = "${text.length}/200"
            }
        })

        // 저장 버튼
        binding.btnNext.setOnClickListener {
            viewModel.saveProfile()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.loadingView.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        viewModel.currentProfile.observe(this) { profile ->
            profile?.let {
                // 프로필 이미지
                it.profileImageUrl?.let { url ->
                    Glide.with(this)
                        .load(url)
                        .placeholder(R.drawable.bg_circle_gray)
                        .into(binding.ivProfile)
                }
                // 닉네임
                binding.etNickname.setText(it.nickname)
                // 성별
                it.sex?.let { sex -> selectGender(sex, updateViewModel = false) }
                // 지역
                it.city?.let { city ->
                    binding.tvRegion.text = city
                    binding.tvRegion.setTextColor(ContextCompat.getColor(this, R.color.black))
                }
                // 자기소개
                it.introduction?.let { intro ->
                    binding.etIntroduction.setText(intro)
                    binding.tvIntroductionCount.text = "${intro.length}/200"
                }
            }
        }

        // 장르 선택 관찰
        viewModel.selectedGenres.observe(this) { genres ->
            updateGenresUI(genres)
        }

        // 악기 선택 관찰
        viewModel.selectedInstruments.observe(this) { instruments ->
            updateInstrumentsUI(instruments)
        }

        viewModel.profileImageUrl.observe(this) { url ->
            url?.let {
                Glide.with(this)
                    .load(it)
                    .placeholder(R.drawable.bg_circle_gray)
                    .into(binding.ivProfile)
            }
        }

        viewModel.isNicknameValid.observe(this) { isValid ->
            updateNicknameStatusUI(isValid)
        }

        viewModel.nicknameStatusMessage.observe(this) { message ->
            binding.tvNicknameStatus.text = message
            binding.tvNicknameStatus.visibility = if (message != null) View.VISIBLE else View.GONE
        }

        viewModel.gender.observe(this) { gender ->
            updateGenderUI(gender)
        }

        viewModel.isChattable.observe(this) { isChattable ->
            binding.ivChattable.setImageResource(
                if (isChattable) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked
            )
        }

        viewModel.isPublicProfile.observe(this) { isPublic ->
            binding.ivPublicProfile.setImageResource(
                if (isPublic) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked
            )
        }

        viewModel.isFormValid.observe(this) { isValid ->
            binding.btnNext.isEnabled = isValid
            binding.btnNext.setBackgroundResource(
                if (isValid) R.drawable.bg_confirm_button else R.drawable.bg_confirm_button_disabled
            )
        }

        viewModel.profileSaveSuccess.observe(this) { success ->
            if (success) {
                preferencesManager.hasProfile = true
                when (settingType) {
                    ProfileSettingType.SETTING -> {
                        // 회원가입 완료 후 메인으로 이동
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }

                    ProfileSettingType.EDIT -> {
                        // 수정 완료
                        Toast.makeText(this, "프로필이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun selectGender(gender: String, updateViewModel: Boolean = true) {
        if (updateViewModel) {
            viewModel.setGender(gender)
        }
    }

    private fun updateGenderUI(gender: String?) {
        val maleSelected = gender == "M"
        val femaleSelected = gender == "F"

        binding.btnGenderMale.setBackgroundResource(
            if (maleSelected) R.drawable.bg_button_yellow_border else R.drawable.bg_auth_text_field
        )
        binding.btnGenderMale.setTextColor(
            ContextCompat.getColor(this, if (maleSelected) R.color.main_yellow else R.color.gray5)
        )

        binding.btnGenderFemale.setBackgroundResource(
            if (femaleSelected) R.drawable.bg_button_yellow_border else R.drawable.bg_auth_text_field
        )
        binding.btnGenderFemale.setTextColor(
            ContextCompat.getColor(this, if (femaleSelected) R.color.main_yellow else R.color.gray5)
        )
    }

    private fun updateNicknameStatusUI(isValid: Boolean?) {
        when (isValid) {
            true -> {
                binding.tvNicknameStatus.setTextColor(ContextCompat.getColor(this, R.color.success_green))
            }

            false -> {
                binding.tvNicknameStatus.setTextColor(ContextCompat.getColor(this, R.color.error_red))
            }

            null -> {
                binding.tvNicknameStatus.visibility = View.GONE
            }
        }
    }

    private fun showRegionSelector() {
        val regions = listOf(
            "서울", "경기", "인천", "부산", "대구", "대전",
            "광주", "울산", "세종", "강원", "충북", "충남",
            "전북", "전남", "경북", "경남", "제주"
        )

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("지역 선택")
            .setItems(regions.toTypedArray()) { _, which ->
                val selectedRegion = regions[which]
                viewModel.setRegion(selectedRegion)
                binding.tvRegion.text = selectedRegion
                binding.tvRegion.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
            .create()
        dialog.show()
    }

    private fun showGenreSelector() {
        showGenreInstrumentSelector(initialTabIndex = 0)
    }

    private fun showInstrumentSelector() {
        showGenreInstrumentSelector(initialTabIndex = 1)
    }

    private fun showGenreInstrumentSelector(initialTabIndex: Int) {
        val dialog = GenreInstrumentSelectDialog.newInstance(
            selectedGenres = viewModel.selectedGenres.value ?: emptySet(),
            selectedInstruments = viewModel.selectedInstruments.value ?: emptySet(),
            initialTabIndex = initialTabIndex
        )

        dialog.onSelectionCompleted = { genres, instruments ->
            viewModel.setSelectedGenres(genres)
            viewModel.setSelectedInstruments(instruments)
        }

        dialog.show(supportFragmentManager, "GenreInstrumentSelectDialog")
    }

    private fun updateGenresUI(genres: Set<Int>) {
        if (genres.isEmpty()) {
            binding.tvGenres.text = "장르"
            binding.tvGenres.setTextColor(ContextCompat.getColor(this, R.color.black))
        } else {
            val names = genres.mapNotNull { Genre.fromCode(it)?.displayName }
            binding.tvGenres.text = "장르: ${names.joinToString(", ")}"
            binding.tvGenres.setTextColor(ContextCompat.getColor(this, R.color.black))
        }
    }

    private fun updateInstrumentsUI(instruments: Set<Int>) {
        if (instruments.isEmpty()) {
            binding.tvInstruments.text = "악기"
            binding.tvInstruments.setTextColor(ContextCompat.getColor(this, R.color.black))
        } else {
            val names = instruments.mapNotNull { Instrument.fromCode(it)?.displayName }
            binding.tvInstruments.text = "악기: ${names.joinToString(", ")}"
            binding.tvInstruments.setTextColor(ContextCompat.getColor(this, R.color.black))
        }
    }

    private fun handleImageSelected(uri: Uri) {
        try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }

            // 이미지 압축
            val resizedBitmap = resizeBitmap(bitmap, 500)

            // userId 가져오기
            val userId = preferencesManager.userId ?: return
            viewModel.uploadProfileImage(resizedBitmap, userId)
        } catch (e: Exception) {
            Toast.makeText(this, "이미지 처리에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        var width = bitmap.width
        var height = bitmap.height

        val ratio = width.toFloat() / height.toFloat()
        if (ratio > 1) {
            width = maxSize
            height = (maxSize / ratio).toInt()
        } else {
            height = maxSize
            width = (maxSize * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    companion object {
        private const val EXTRA_IS_EDIT_MODE = "is_edit_mode"

        fun newIntent(context: Context, isEditMode: Boolean = false): Intent {
            return Intent(context, ProfileSettingActivity::class.java).apply {
                putExtra(EXTRA_IS_EDIT_MODE, isEditMode)
            }
        }
    }
}
