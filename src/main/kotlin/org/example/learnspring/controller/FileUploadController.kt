package org.example.learnspring.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths

@RestController
@RequestMapping("/api/files")
class FileUploadController {

    @PostMapping("/upload")
    fun handleFileUpload(@RequestParam("file") file: MultipartFile): String {
        val uploadPath = Paths.get("uploads/${file.originalFilename}")
        Files.createDirectories(uploadPath.parent)
        file.inputStream.use { Files.copy(it, uploadPath) }
        return "File uploaded: ${file.originalFilename}"
    }
}