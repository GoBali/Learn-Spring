package org.example.learnspring.dto

import java.time.OffsetDateTime

data class MessageDto(
    val content: String? = null
)

data class FileUploadResponseDto(
    val success: Boolean,
    val message: String,
    val filename: String?
)

data class ErrorResponseDto(
    val timestamp: OffsetDateTime = OffsetDateTime.now(),
    val errorCode: String,
    val error: String,
    val errors: Map<String, String>? = null
)