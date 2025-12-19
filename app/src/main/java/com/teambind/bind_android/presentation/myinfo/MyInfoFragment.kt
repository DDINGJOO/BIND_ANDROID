package com.teambind.bind_android.presentation.myinfo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.teambind.bind_android.R
import com.teambind.bind_android.databinding.FragmentMyinfoBinding
import com.teambind.bind_android.presentation.base.BaseFragment
import com.teambind.bind_android.presentation.customerservice.CustomerServiceActivity
import com.teambind.bind_android.presentation.minifeed.MiniFeedActivity
import com.teambind.bind_android.presentation.myposts.MyPostsActivity
import com.teambind.bind_android.presentation.noticeevent.NoticeEventActivity
import com.teambind.bind_android.presentation.reservationhistory.ReservationHistoryActivity
import com.teambind.bind_android.presentation.settings.SettingsActivity
import com.teambind.bind_android.presentation.start.auth.AuthMainActivity
import com.teambind.bind_android.presentation.start.profilesetting.ProfileSettingActivity
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import com.teambind.bind_android.util.extension.startActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyInfoFragment : BaseFragment<FragmentMyinfoBinding>() {

    private val viewModel: MyInfoViewModel by viewModels()

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentMyinfoBinding {
        return FragmentMyinfoBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        setupMenuItems()
        setupClickListeners()
    }

    override fun initObserver() {
        // 프로필 데이터 관찰
        collectLatestFlow(viewModel.uiState) { state ->
            state.profile?.let { profile ->
                updateProfileUI(profile)
            }

            // 예약 수 업데이트
            binding.tvReservationCount.text = state.reservationCount.toString()

            // 에러 메시지 표시
            state.errorMessage?.let {
                showToast(it)
                viewModel.clearError()
            }
        }

        // 로그아웃 상태 관찰
        collectLatestFlow(viewModel.logoutState) { isLoggedOut ->
            if (isLoggedOut) {
                startActivity<AuthMainActivity> {
                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                            android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            }
        }
    }

    private fun updateProfileUI(profile: com.teambind.bind_android.data.model.response.ProfileDto) {
        with(binding) {
            // 닉네임
            tvNickname.text = "${profile.nickname}님"

            // 이메일 대신 소개글 또는 지역 표시
            tvEmail.text = profile.introduction ?: profile.city ?: ""

            // 프로필 이미지
            if (!profile.profileImageUrl.isNullOrEmpty()) {
                Glide.with(ivProfile)
                    .load(profile.profileImageUrl)
                    .placeholder(R.drawable.bg_circle_gray)
                    .circleCrop()
                    .into(ivProfile)
            } else {
                ivProfile.setImageResource(R.drawable.bg_circle_gray)
            }
        }
    }

    private fun setupMenuItems() {
        with(binding) {
            // Notice/Event
            menuNotice.ivMenuIcon.setImageResource(R.drawable.ic_notice)
            menuNotice.tvMenuTitle.text = "공지사항/이벤트"

            // Customer Service
            menuCustomerService.ivMenuIcon.setImageResource(R.drawable.ic_customer_service)
            menuCustomerService.tvMenuTitle.text = "고객센터"

            // Terms
            menuTerms.ivMenuIcon.setImageResource(R.drawable.ic_docs)
            menuTerms.tvMenuTitle.text = "약관정보"

            // Version Info
            menuVersion.ivMenuIcon.setImageResource(R.drawable.ic_info)
            menuVersion.tvMenuTitle.text = "버전 정보"
            menuVersion.tvMenuValue.visibility = View.VISIBLE
            menuVersion.tvMenuValue.text = "1.0.0"
        }
    }

    private fun setupClickListeners() {
        with(binding) {
            // Settings button
            btnSettings.setOnSingleClickListener {
                SettingsActivity.start(requireContext())
            }

            // Profile image - 프로필 편집 화면으로 이동
            layoutProfileImage.setOnSingleClickListener {
                startActivity(ProfileSettingActivity.newIntent(requireContext(), isEditMode = true))
            }

            // Mini feed button
            btnMiniFeed.setOnSingleClickListener {
                viewModel.uiState.value.profile?.let { profile ->
                    MiniFeedActivity.start(requireContext(), profile.userId)
                }
            }

            // My reservation
            btnMyReservation.setOnSingleClickListener {
                ReservationHistoryActivity.start(requireContext())
            }

            // My posts
            btnMyPost.setOnSingleClickListener {
                MyPostsActivity.start(requireContext())
            }

            // Notice/Event
            menuNotice.root.setOnSingleClickListener {
                NoticeEventActivity.start(requireContext())
            }

            // Customer Service
            menuCustomerService.root.setOnSingleClickListener {
                CustomerServiceActivity.start(requireContext())
            }

            // Terms
            menuTerms.root.setOnSingleClickListener {
                // TODO: Navigate to terms
                showToast("약관정보")
            }

            // Version
            menuVersion.root.setOnSingleClickListener {
                // TODO: Show version info
                showToast("버전 정보")
            }
        }
    }
}
