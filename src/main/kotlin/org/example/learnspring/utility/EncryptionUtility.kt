package org.example.learnspring.utility

import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

@Component
class EncryptionUtility(
    private val secretKey: String = System.getenv("APP_SECRET_KEY") ?: "DefaultKey-NeedChangeInProduction"
) {
    init {
        require(secretKey.length >= 16) { "비밀키는 최소 16자 이상이어야 합니다" }
    }

    // AES 암호화
    fun encrypt(data: String): String {
        val key = generateKey(secretKey)
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedBytes = cipher.doFinal(data.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    // AES 복호화
    fun decrypt(encryptedData: String): String {
        val key = generateKey(secretKey)
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decodedBytes = Base64.getDecoder().decode(encryptedData)
        val decryptedBytes = cipher.doFinal(decodedBytes)
        return String(decryptedBytes)
    }

    // SHA-256 해시
    fun hash(data: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(data.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private fun generateKey(key: String): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(key.toByteArray())
        return SecretKeySpec(keyBytes.copyOf(16), "AES")
    }
}