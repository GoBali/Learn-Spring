package org.example.learnspring.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.Instant

@RestController
@RequestMapping("/api/files")
class FileUploadController {

    companion object {
        private const val UPLOAD_DIR = "uploads"
    }

    @PostMapping("/upload")
    fun handleFileUpload(@RequestParam("file") file: MultipartFile): ResponseEntity<String> {
        if (file.isEmpty) {
            return ResponseEntity("File is empty", HttpStatus.BAD_REQUEST)
        }

        val filename = generateFilename(file.originalFilename!!)
        val uploadPath = Paths.get("$UPLOAD_DIR/${filename}")

        return try {
            Files.createDirectories(uploadPath.parent)
            file.inputStream.use {
                Files.copy(it, uploadPath, StandardCopyOption.REPLACE_EXISTING)
            }
            ResponseEntity("File uploaded: ${file.originalFilename}", HttpStatus.OK)
        } catch (e: Exception) {
            ResponseEntity("Error uploading file: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    private fun generateFilename(originalFilename: String): String {
        val timestamp = Instant.now().epochSecond
        return "${timestamp}_${originalFilename}"
    }
}