package org.example.learnspring.controller

import mu.KotlinLogging
import org.example.learnspring.dto.MessageDto
import org.example.learnspring.utility.EncryptionUtility
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class TestController(
    private val encryptionUtility: EncryptionUtility
) {
    private val logger = KotlinLogging.logger {}

    @GetMapping("/hello")
    fun hello(): ResponseEntity<Map<String, String>> {
        logger.info("Called hello endpoint")
        return ResponseEntity.ok(mapOf("message" to "Hello, World!"))
    }

    @PostMapping("/encrypt")
    fun encrypt(@RequestBody message: MessageDto): ResponseEntity<Map<String, String>> {
        logger.info("Called encrypt endpoint - Message length: ${message.content?.length ?: 0}")
        return try {
            val encrypted = message.content?.let {
                encryptionUtility.encrypt(it)
            } ?: throw IllegalArgumentException("Message is empty")
            ResponseEntity.ok(mapOf("encrypted" to encrypted))
        } catch (e: Exception) {
            logger.error("Encryption error", e)
            ResponseEntity.badRequest().body(mapOf("error" to e.message.toString()))
        }
    }

    @PostMapping("/decrypt")
    fun decrypt(@RequestBody message: MessageDto): ResponseEntity<Map<String, String>> {
        logger.info("Decrypt request")
        return try {
            val decrypted = message.content?.let {
                encryptionUtility.decrypt(it)
            } ?: throw IllegalArgumentException("Message is empty")
            ResponseEntity.ok(mapOf("decrypted" to decrypted))
        } catch (e: Exception) {
            logger.error("Decryption error", e)
            ResponseEntity.badRequest().body(mapOf("error" to e.message.toString()))
        }
    }
}