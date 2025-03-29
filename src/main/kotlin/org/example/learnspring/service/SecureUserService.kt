package org.example.learnspring.service

import io.micrometer.core.instrument.MeterRegistry
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor
import org.example.learnspring.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import mu.KotlinLogging
import org.example.learnspring.exception.UserNotFoundException


@Service
class SecureUserService(
    private val userRepository: UserRepository,
    private val meterRegistry: MeterRegistry
) {
    private val logger = KotlinLogging.logger {}

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
                val message = "User with ID $id not found"
                logger.error(message)
                throw UserNotFoundException(message)
            }
            textEncryptor.encrypt(user.toString())
        } ?: run {
            val message = "Timer did not return a value"
            logger.error(message)
            throw IllegalStateException(message)
        }
    }
}
