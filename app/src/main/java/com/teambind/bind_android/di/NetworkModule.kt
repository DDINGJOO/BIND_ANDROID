package com.teambind.bind_android.di

import com.teambind.bind_android.data.api.interceptor.AuthInterceptor
import com.teambind.bind_android.data.api.interceptor.TokenRefreshAuthenticator
import com.teambind.bind_android.data.api.service.AuthService
import com.teambind.bind_android.data.api.service.CommunityService
import com.teambind.bind_android.data.api.service.EnumsService
import com.teambind.bind_android.data.api.service.FeedService
import com.teambind.bind_android.data.api.service.HomeService
import com.teambind.bind_android.data.api.service.ReportService
import com.teambind.bind_android.data.api.service.ImageService
import com.teambind.bind_android.data.api.service.PaymentService
import com.teambind.bind_android.data.api.service.PricingPolicyService
import com.teambind.bind_android.data.api.service.ProductService
import com.teambind.bind_android.data.api.service.ProfileService
import com.teambind.bind_android.data.api.service.ReservationService
import com.teambind.bind_android.data.api.service.SignUpService
import com.teambind.bind_android.data.api.service.StudioService
import com.teambind.bind_android.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenRefreshAuthenticator: TokenRefreshAuthenticator
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor { chain ->
                // 캐시 방지: 항상 최신 데이터 요청
                val request = chain.request().newBuilder()
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Pragma", "no-cache")
                    .build()
                chain.proceed(request)
            }
            .authenticator(tokenRefreshAuthenticator)
            .connectTimeout(Constants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    // 기본 API용 Retrofit
    @Provides
    @Singleton
    @Named("base")
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 이미지 업로드용 Retrofit
    @Provides
    @Singleton
    @Named("imageUpload")
    fun provideImageUploadRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.IMAGE_UPLOAD_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 결제용 Retrofit
    @Provides
    @Singleton
    @Named("payment")
    fun providePaymentRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.PAYMENT_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthService(@Named("base") retrofit: Retrofit): AuthService {
        return retrofit.create(AuthService::class.java)
    }

    @Provides
    @Singleton
    fun provideSignUpService(@Named("base") retrofit: Retrofit): SignUpService {
        return retrofit.create(SignUpService::class.java)
    }

    @Provides
    @Singleton
    fun provideCommunityService(@Named("base") retrofit: Retrofit): CommunityService {
        return retrofit.create(CommunityService::class.java)
    }

    @Provides
    @Singleton
    fun provideProfileService(@Named("base") retrofit: Retrofit): ProfileService {
        return retrofit.create(ProfileService::class.java)
    }

    @Provides
    @Singleton
    fun provideStudioService(@Named("base") retrofit: Retrofit): StudioService {
        return retrofit.create(StudioService::class.java)
    }

    @Provides
    @Singleton
    fun provideReservationService(@Named("base") retrofit: Retrofit): ReservationService {
        return retrofit.create(ReservationService::class.java)
    }

    @Provides
    @Singleton
    fun provideHomeService(@Named("base") retrofit: Retrofit): HomeService {
        return retrofit.create(HomeService::class.java)
    }

    @Provides
    @Singleton
    fun provideImageService(@Named("imageUpload") retrofit: Retrofit): ImageService {
        return retrofit.create(ImageService::class.java)
    }

    @Provides
    @Singleton
    fun providePricingPolicyService(@Named("base") retrofit: Retrofit): PricingPolicyService {
        return retrofit.create(PricingPolicyService::class.java)
    }

    @Provides
    @Singleton
    fun provideProductService(@Named("base") retrofit: Retrofit): ProductService {
        return retrofit.create(ProductService::class.java)
    }

    @Provides
    @Singleton
    fun providePaymentService(@Named("payment") retrofit: Retrofit): PaymentService {
        return retrofit.create(PaymentService::class.java)
    }

    @Provides
    @Singleton
    fun provideEnumsService(@Named("base") retrofit: Retrofit): EnumsService {
        return retrofit.create(EnumsService::class.java)
    }

    @Provides
    @Singleton
    fun provideFeedService(@Named("base") retrofit: Retrofit): FeedService {
        return retrofit.create(FeedService::class.java)
    }

    @Provides
    @Singleton
    fun provideReportService(@Named("base") retrofit: Retrofit): ReportService {
        return retrofit.create(ReportService::class.java)
    }
}
