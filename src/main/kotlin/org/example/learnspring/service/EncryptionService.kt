package org.example.learnspring.service

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service


@Service
class EncryptionService {
    @Value("\${encryption.key}")
    private lateinit var encryptionKey: String

    private val textEncryptor: StandardPBEStringEncryptor by lazy {
        StandardPBEStringEncryptor().apply {
            setPassword(encryptionKey) // 환경 변수 또는 설정 파일에서 암호화 키 로드
            setAlgorithm("PBEWithMD5AndDES") // 신뢰할 수 있는 기본 알고리즘 사용
        }
    }

    fun encryptData(data: String): String {
        return textEncryptor.encrypt(data)
    }

    fun decryptData(encryptedData: String): String {
        return textEncryptor.decrypt(encryptedData)
    }

    fun getDecryptedSensitiveData(): String {
        return decryptData(encryptionKey)
    }
}