package org.example.learnspring.service

import io.micrometer.core.instrument.MeterRegistry
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor
import org.example.learnspring.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SecureUserService(
    private val userRepository: UserRepository,
    private val meterRegistry: MeterRegistry
) {
    @Value("\${encryption.key}")
    private lateinit var encryptionKey: String

    private val textEncryptor: StandardPBEStringEncryptor by lazy {
        StandardPBEStringEncryptor().apply {
            setPassword(encryptionKey) // 설정된 암호화 키 사용
            setAlgorithm("PBEWithMD5AndDES") // 권장 기본 알고리즘
        }
    }

    companion object {
        const val TIMER_NAME = "service.getSecureUserById.timer"
        const val ERROR_COUNTER_NAME = "service.getSecureUserById.error.count"
    }

    fun getSecureUserById(id: Long): String {
        val timer = meterRegistry.timer(TIMER_NAME)
        return timer.recordCallable {
            val user = userRepository.findById(id).orElseThrow {
                meterRegistry.counter(ERROR_COUNTER_NAME).increment()
                throw UserNotFoundException("User with ID $id not found")
            }
            textEncryptor.encrypt(user.toString())
        } ?: throw IllegalStateException("Timer did not return a value")
    }
}

class UserNotFoundException(message: String) : RuntimeException(message)