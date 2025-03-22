package org.example.learnspring.service

import org.example.learnspring.utility.EncryptionUtility
import org.springframework.stereotype.Service

@Service
class MessageService(private val encryptionUtility: EncryptionUtility) {
    fun encryptMessage(message: String): String {
        return encryptionUtility.encrypt(message)
    }

    fun decryptMessage(encryptedMessage: String): String {
        return encryptionUtility.decrypt(encryptedMessage)
    }

    fun hashMessage(message: String): String {
        return encryptionUtility.hash(message)
    }
}