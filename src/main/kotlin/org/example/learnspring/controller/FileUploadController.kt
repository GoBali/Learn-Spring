package org.example.learnspring.controller

import org.example.learnspring.dto.FileUploadResponseDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.unit.DataSize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.Instant

@RestController
@RequestMapping("/api/files")
class FileUploadController(
    @Value("\${file.upload.directory:uploads}") private var
    uploadDir: String,
    @Value("\${file.upload.max-file-size:10MB}") private val
    maxFileSize: DataSize,
    @Value("\${file.upload.allowed-types:image/jpeg,image/png,application/pdf}") private val
    allowedTypes: List<String>
) {
    private val logger = mu.KotlinLogging.logger {}
    private val uploadPath: Path = Paths.get(uploadDir)
        .toAbsolutePath().normalize()

    init {
        try {
            Files.createDirectories(uploadPath)
            logger.info("Upload directory created: $uploadPath")
        } catch (e: Exception) {
            logger.error("Failed to create upload directory: $uploadPath", e)
        }
    }

    @PostMapping("/upload")
    fun handleFileUpload(@RequestParam("file") file: MultipartFile): ResponseEntity<FileUploadResponseDto> {
        if (file.isEmpty) {
            val response = FileUploadResponseDto(false, "File is empty", null)
            return ResponseEntity.badRequest().body(response)
        }

        if (file.size > maxFileSize.toBytes()) {
            val response = FileUploadResponseDto(false, "File size exceeds the limit", null)
            return ResponseEntity.badRequest().body(response)
        }

        val contentType = file.contentType ?: ""
        if (!allowedTypes.contains(contentType)) {
            val response = FileUploadResponseDto(false, "File type is not allowed", null)
            return ResponseEntity.badRequest().body(response)
        }

        val originalFilename = file.originalFilename ?: "unknown_file"
        val safeFilename = generateFilename(originalFilename)
        val targetFilePath = uploadPath.resolve(safeFilename)

        try {
            Files.copy(file.inputStream, targetFilePath, StandardCopyOption.REPLACE_EXISTING)
            logger.info("File uploaded successfully: $safeFilename")

            val response = FileUploadResponseDto(
                success = true,
                message = "File uploaded successfully",
                filename = safeFilename
            )
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Failed to upload file: $safeFilename", e)

            val response = FileUploadResponseDto(
                success = false,
                message = "Failed to upload file: ${e.message}",
                filename = null
            )
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
        }
    }

    private fun generateFilename(originalFilename: String): String {
        val timestamp = Instant.now().epochSecond
        return "${timestamp}_${originalFilename}"
    }
}