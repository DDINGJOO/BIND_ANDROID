package com.teambind.bind_android.data.model

// 이미지 업로드 목적
enum class ImageUploadPurpose(val value: String) {
    PROFILE("PROFILE"),
    POST("POST"),
    BAND_ROOM("BAND_ROOM"),
    BUSINESS_DOC("BUSINESS_DOC"),
    ARTICLE("ARTICLE"),
    EVENT("EVENT"),
    IMAGE("IMAGE"),
    VIDEO("VIDEO"),
    AUDIO("AUDIO"),
    DOCUMENT("DOCUMENT"),
    OTHER("OTHER"),
    STUDIO("STUDIO")
}

// 이미지 공개 범위
enum class VisibilityScope(val value: String) {
    PUBLIC("PUBLIC"),
    PRIVATE("PRIVATE")
}
