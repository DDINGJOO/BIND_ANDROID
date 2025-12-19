package com.teambind.bind_android.data.api.service

import com.teambind.bind_android.data.model.response.ImageUploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ImageService {

    // 단일 이미지 업로드 (프로필 이미지 등)
    @Multipart
    @POST("images")
    suspend fun uploadImage(
        @Part("category") category: RequestBody,
        @Part("uploaderId") uploaderId: RequestBody,
        @Part image: MultipartBody.Part
    ): ImageUploadResponse

    // 다중 이미지 업로드 (게시글 이미지 등)
    @Multipart
    @POST("images")
    suspend fun uploadImages(
        @Part("category") category: RequestBody,
        @Part("uploaderId") uploaderId: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): List<ImageUploadResponse>
}
