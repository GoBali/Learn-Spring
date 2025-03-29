package org.example.learnspring.service

import org.example.learnspring.dto.FileUploadResponseDto
import org.example.learnspring.exception.FileUploadException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.util.unit.DataSize
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.Instant

@Service
class FileUploadService(
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

    fun handleFileUpload(file: MultipartFile): FileUploadResponseDto {
        if (file.isEmpty) {
            throw FileUploadException("File is empty")
        }

        if (file.size > maxFileSize.toBytes()) {
            throw FileUploadException("File size exceeds the limit")
        }

        val contentType = file.contentType ?: ""
        if (!allowedTypes.contains(contentType)) {
            throw FileUploadException("File type is not allowed")
        }

        val originalFilename = file.originalFilename ?: "unknown_file"
        val safeFilename = generateFilename(originalFilename)
        val targetFilePath = uploadPath.resolve(safeFilename)

        try {
            Files.copy(file.inputStream, targetFilePath, StandardCopyOption.REPLACE_EXISTING)
            logger.info("File uploaded successfully: $safeFilename")

            return FileUploadResponseDto(
                success = true,
                message = "File uploaded successfully",
                filename = safeFilename
            )
        } catch (e: Exception) {
            logger.error("Failed to upload file: $safeFilename", e)
            throw FileUploadException("Failed to upload file: $safeFilename", e)
        }
    }

    private fun generateFilename(originalFilename: String): String {
        val timestamp = Instant.now().epochSecond
        return "${timestamp}_${originalFilename}"
    }
}