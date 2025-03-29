package org.example.learnspring.dto

data class MessageDto(
    val content: String? = null
)

data class FileUploadResponseDto(
    val success: Boolean,
    val message: String,
    val filename: String?
)