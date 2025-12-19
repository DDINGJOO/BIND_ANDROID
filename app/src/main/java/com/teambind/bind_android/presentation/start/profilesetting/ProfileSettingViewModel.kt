package com.teambind.bind_android.presentation.start.profilesetting

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.model.Genre
import com.teambind.bind_android.data.model.Instrument
import com.teambind.bind_android.data.model.Region
import com.teambind.bind_android.data.model.request.CreateProfileRequest
import com.teambind.bind_android.data.model.request.EditProfileRequest
import com.teambind.bind_android.data.model.response.ProfileDto
import com.teambind.bind_android.data.repository.ImageRepository
import com.teambind.bind_android.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ProfileSettingType {
    SETTING,  // 회원가입 후 최초 프로필 설정
    EDIT      // 프로필 수정
}

@HiltViewModel
class ProfileSettingViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {

    // UI State
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _profileSaveSuccess = MutableLiveData<Boolean>()
    val profileSaveSuccess: LiveData<Boolean> = _profileSaveSuccess

    private val _currentProfile = MutableLiveData<ProfileDto?>()
    val currentProfile: LiveData<ProfileDto?> = _currentProfile

    // Form data
    private val _profileImageUrl = MutableLiveData<String?>()
    val profileImageUrl: LiveData<String?> = _profileImageUrl

    private val _profileImageId = MutableLiveData<String?>()

    private val _nickname = MutableLiveData("")
    val nickname: LiveData<String> = _nickname

    private var originalNickname: String = ""  // 원본 닉네임 (edit 모드에서 변경 감지용)

    private val _isNicknameValid = MutableLiveData<Boolean?>(null)
    val isNicknameValid: LiveData<Boolean?> = _isNicknameValid

    private val _nicknameStatusMessage = MutableLiveData<String?>()
    val nicknameStatusMessage: LiveData<String?> = _nicknameStatusMessage

    private val _gender = MutableLiveData<String?>()
    val gender: LiveData<String?> = _gender

    private val _region = MutableLiveData<String?>()
    val region: LiveData<String?> = _region

    private val _regionCode = MutableLiveData<String?>()  // 서버로 보낼 지역 코드

    private val _isChattable = MutableLiveData(false)
    val isChattable: LiveData<Boolean> = _isChattable

    private val _isPublicProfile = MutableLiveData(false)
    val isPublicProfile: LiveData<Boolean> = _isPublicProfile

    private val _isFormValid = MutableLiveData(false)
    val isFormValid: LiveData<Boolean> = _isFormValid

    // 장르/악기 선택
    private val _selectedGenres = MutableLiveData<Set<Int>>(emptySet())
    val selectedGenres: LiveData<Set<Int>> = _selectedGenres

    private val _selectedInstruments = MutableLiveData<Set<Int>>(emptySet())
    val selectedInstruments: LiveData<Set<Int>> = _selectedInstruments

    // 자기소개
    private val _introduction = MutableLiveData("")
    val introduction: LiveData<String> = _introduction

    // Setting type
    private var settingType: ProfileSettingType = ProfileSettingType.SETTING

    fun setSettingType(type: ProfileSettingType) {
        settingType = type
    }

    // 내 프로필 조회
    fun fetchMyProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = profileRepository.getMyProfile()
            _isLoading.value = false

            result.onSuccess { response ->
                _currentProfile.value = response.profile
                // 기존 프로필 데이터로 폼 초기화
                response.profile.let { profile ->
                    _profileImageUrl.value = profile.profileImageUrl
                    _nickname.value = profile.nickname
                    originalNickname = profile.nickname  // 원본 닉네임 저장
                    _gender.value = profile.sex
                    _region.value = profile.city
                    // city를 코드로 변환해서 저장
                    _regionCode.value = profile.city?.let { Region.getCodeFromDisplayName(it) }
                    _isChattable.value = profile.isChattable
                    _isPublicProfile.value = profile.isPublic
                    _isNicknameValid.value = true // 기존 닉네임은 유효함
                    // 장르/악기 저장
                    _selectedGenres.value = Genre.getCodesFromServerKeys(profile.genres).toSet()
                    _selectedInstruments.value = Instrument.getCodesFromServerKeys(profile.instruments).toSet()
                    _introduction.value = profile.introduction ?: ""
                }
                validateForm()
            }.onFailure { error ->
                _errorMessage.value = error.message
            }
        }
    }

    // 닉네임 변경
    fun setNickname(value: String) {
        _nickname.value = value
        _isNicknameValid.value = null // 닉네임 변경 시 검증 상태 초기화
        _nicknameStatusMessage.value = null
        validateForm()
    }

    // 닉네임 중복 확인
    fun checkNicknameAvailable() {
        val currentNickname = _nickname.value ?: return
        if (currentNickname.isEmpty()) {
            _nicknameStatusMessage.value = "닉네임을 입력해주세요."
            _isNicknameValid.value = false
            return
        }
        if (currentNickname.length < 2) {
            _nicknameStatusMessage.value = "닉네임은 2자 이상이어야 합니다."
            _isNicknameValid.value = false
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = profileRepository.checkNicknameAvailable(currentNickname)
            _isLoading.value = false

            result.onSuccess { isAvailable ->
                // validate API에서 false = 사용 가능 (중복 아님), true = 중복
                if (!isAvailable) {
                    _isNicknameValid.value = true
                    _nicknameStatusMessage.value = "사용 가능한 닉네임입니다."
                } else {
                    _isNicknameValid.value = false
                    _nicknameStatusMessage.value = "이미 사용 중인 닉네임입니다."
                }
                validateForm()
            }.onFailure { error ->
                _isNicknameValid.value = false
                _nicknameStatusMessage.value = error.message ?: "닉네임 확인에 실패했습니다."
            }
        }
    }

    // 성별 선택
    fun setGender(value: String) {
        _gender.value = value
        validateForm()
    }

    // 지역 선택 (displayName으로 받아서 code로 저장)
    fun setRegion(displayName: String) {
        _region.value = displayName
        _regionCode.value = Region.getCodeFromDisplayName(displayName)
        validateForm()
    }

    // 채팅 허용 토글
    fun toggleChattable() {
        _isChattable.value = !(_isChattable.value ?: false)
    }

    // 프로필 공개 토글
    fun togglePublicProfile() {
        _isPublicProfile.value = !(_isPublicProfile.value ?: false)
    }

    // 장르 선택
    fun setSelectedGenres(genres: Set<Int>) {
        _selectedGenres.value = genres
    }

    // 악기 선택
    fun setSelectedInstruments(instruments: Set<Int>) {
        _selectedInstruments.value = instruments
    }

    // 자기소개 설정
    fun setIntroduction(value: String) {
        _introduction.value = value
    }

    // 프로필 이미지 업로드
    fun uploadProfileImage(bitmap: Bitmap, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = profileRepository.uploadProfileImage(bitmap, userId)
            _isLoading.value = false

            result.onSuccess { response ->
                _profileImageUrl.value = response.imageUrl
                _profileImageId.value = response.imageId
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "이미지 업로드에 실패했습니다."
            }
        }
    }

    // 폼 유효성 검사
    private fun validateForm() {
        val isValid = !_nickname.value.isNullOrEmpty() &&
                _isNicknameValid.value == true &&
                !_gender.value.isNullOrEmpty() &&
                !_region.value.isNullOrEmpty()

        _isFormValid.value = isValid
    }

    // 프로필 저장
    fun saveProfile() {
        // edit 모드에서는 항상 저장 가능
        if (settingType == ProfileSettingType.SETTING && _isFormValid.value != true) {
            _errorMessage.value = "모든 필수 항목을 입력해주세요."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            val result = when (settingType) {
                ProfileSettingType.SETTING -> {
                    // 신규 프로필 생성
                    val request = CreateProfileRequest(
                        nickname = _nickname.value ?: "",
                        sex = _gender.value,
                        city = _regionCode.value ?: "ETC",
                        profileImageUrl = _profileImageUrl.value,
                        introduction = null,
                        genres = emptyList(),
                        instruments = emptyList()
                    )
                    profileRepository.createProfile(request)
                }
                ProfileSettingType.EDIT -> {
                    // 기존 프로필 수정
                    // 닉네임이 원본과 같으면 null로 전송 (서버에서 무시)
                    val currentNickname = _nickname.value ?: ""
                    val nicknameToSend = if (currentNickname == originalNickname) null else currentNickname

                    val request = EditProfileRequest(
                        nickname = nicknameToSend,
                        sex = _gender.value,
                        city = _regionCode.value ?: "ETC",
                        isChattable = _isChattable.value ?: false,
                        isPublic = _isPublicProfile.value ?: false,
                        profileImageId = _profileImageId.value ?: "",
                        introduction = _introduction.value,
                        genres = _selectedGenres.value?.toList() ?: emptyList(),
                        instruments = _selectedInstruments.value?.toList() ?: emptyList()
                    )
                    profileRepository.editProfile(request)
                }
            }

            _isLoading.value = false

            result.onSuccess {
                _profileSaveSuccess.value = true
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "프로필 저장에 실패했습니다."
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
