package org.example.learnspring.exception

import mu.KotlinLogging
import org.example.learnspring.dto.ErrorResponseDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {
    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException):
            ResponseEntity<ErrorResponseDto> {
        logger.error("Illegal argument error", ex)
        val errorResponse = ErrorResponseDto(
            errorCode = "ILLEGAL_ARGUMENT",
            error = ex.message ?: "Invalid request."
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception):
            ResponseEntity<ErrorResponseDto> {
        logger.error("Generic internal server error", ex)
        val errorResponse = ErrorResponseDto(
            errorCode = "INTERNAL_SERVER_ERROR",
            error = ex.message ?: "An internal server error occurred."
        )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException):
            ResponseEntity<ErrorResponseDto> {
        val errors = ex.bindingResult.fieldErrors.associate {
            it.field to (it.defaultMessage ?: "Invalid value.")
        }
        val errorResponse = ErrorResponseDto(
            errorCode = "VALIDATION_ERROR",
            error = "Validation failed for input parameters.",
            errors = errors
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException): ResponseEntity<ErrorResponseDto> {
        logger.error("Runtime exception occurred", ex)
        val errorResponse = ErrorResponseDto(
            errorCode = "RUNTIME_EXCEPTION",
            error = ex.message ?: "Unexpected runtime error occurred."
        )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(ex: AuthenticationException):
            ResponseEntity<ErrorResponseDto> {
        val errorResponse = ErrorResponseDto(
            errorCode = "UNAUTHORIZED",
            error = ex.message ?: "Authentication failed."
        )
        return ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(FileUploadException::class)
    fun handleFileUploadException(ex: FileUploadException):
            ResponseEntity<ErrorResponseDto> {
        val errorResponse = ErrorResponseDto(
            errorCode = "FILE_UPLOAD_FAILED",
            error = ex.message ?: "File upload failed."
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }
}

class UserNotFoundException(message: String) : RuntimeException(message)

class AuthenticationException(message: String) : RuntimeException(message)

class FileUploadException: RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}