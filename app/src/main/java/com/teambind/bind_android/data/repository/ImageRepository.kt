package com.teambind.bind_android.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.teambind.bind_android.data.api.service.ImageService
import com.teambind.bind_android.data.model.ImageUploadPurpose
import com.teambind.bind_android.data.model.response.ImageUploadResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepository @Inject constructor(
    private val imageService: ImageService,
    @ApplicationContext private val context: Context
) {

    // 단일 이미지 업로드
    suspend fun uploadImage(
        imageUri: Uri,
        purpose: ImageUploadPurpose,
        uploaderId: String
    ): Result<ImageUploadResponse> {
        return try {
            val file = uriToFile(imageUri)
            val compressedFile = compressImage(file)

            val categoryBody = purpose.value.toRequestBody("text/plain".toMediaTypeOrNull())
            val uploaderIdBody = uploaderId.toRequestBody("text/plain".toMediaTypeOrNull())

            val requestFile = compressedFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", compressedFile.name, requestFile)

            val response = imageService.uploadImage(categoryBody, uploaderIdBody, imagePart)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Bitmap으로 이미지 업로드 (갤러리에서 선택한 경우)
    suspend fun uploadImage(
        bitmap: Bitmap,
        fileName: String,
        purpose: ImageUploadPurpose,
        uploaderId: String
    ): Result<ImageUploadResponse> {
        return try {
            val file = bitmapToFile(bitmap, fileName)
            val compressedFile = compressImage(file)

            val categoryBody = purpose.value.toRequestBody("text/plain".toMediaTypeOrNull())
            val uploaderIdBody = uploaderId.toRequestBody("text/plain".toMediaTypeOrNull())

            val requestFile = compressedFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", compressedFile.name, requestFile)

            val response = imageService.uploadImage(categoryBody, uploaderIdBody, imagePart)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 다중 이미지 업로드
    suspend fun uploadImages(
        imageUris: List<Uri>,
        purpose: ImageUploadPurpose,
        uploaderId: String
    ): Result<List<ImageUploadResponse>> {
        return try {
            val categoryBody = purpose.value.toRequestBody("text/plain".toMediaTypeOrNull())
            val uploaderIdBody = uploaderId.toRequestBody("text/plain".toMediaTypeOrNull())

            val imageParts = imageUris.mapIndexed { index, uri ->
                val file = uriToFile(uri)
                val compressedFile = compressImage(file)
                val requestFile = compressedFile.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("images", compressedFile.name, requestFile)
            }

            val response = imageService.uploadImages(categoryBody, uploaderIdBody, imageParts)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Uri를 File로 변환
    private fun uriToFile(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

    // Bitmap을 File로 변환
    private fun bitmapToFile(bitmap: Bitmap, fileName: String): File {
        val file = File(context.cacheDir, "${fileName}_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
        }
        return file
    }

    // 이미지 압축 (iOS의 compressionQuality: 0.7 에 맞춤)
    private fun compressImage(file: File, quality: Int = 70): File {
        val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

        val compressedFile = File(context.cacheDir, "compressed_${file.name}")
        FileOutputStream(compressedFile).use { output ->
            output.write(outputStream.toByteArray())
        }
        return compressedFile
    }
}
