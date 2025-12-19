package com.teambind.bind_android.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Repository Module
 *
 * Repository 클래스들은 @Singleton과 @Inject constructor를 사용하여
 * Hilt가 자동으로 주입합니다.
 *
 * - AuthRepository: 로그인/토큰 관리
 * - SignUpRepository: 회원가입
 * - ProfileRepository: 프로필 조회/수정
 * - ImageRepository: 이미지 업로드
 * - CommunityRepository: 커뮤니티 게시글/댓글
 * - StudioRepository: 스튜디오 조회/검색
 * - ReservationRepository: 예약 관리
 * - HomeRepository: 홈 배너
 * - PricingPolicyRepository: 가격 정책 조회
 * - ProductRepository: 상품 가용성 조회
 * - PaymentRepository: 결제 확인
 * - EnumsRepository: 지역/키워드 조회
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule
