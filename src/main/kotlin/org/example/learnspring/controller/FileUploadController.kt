package org.example.learnspring.controller

import org.example.learnspring.dto.FileUploadResponseDto
import org.example.learnspring.service.FileUploadService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/files")
class FileUploadController(private val fileUploadService: FileUploadService) {
    @PostMapping("/upload")
    fun handleFileUpload(@RequestParam("file") file: MultipartFile): ResponseEntity<FileUploadResponseDto> {
        return ResponseEntity.ok(fileUploadService.handleFileUpload(file))
    }
}