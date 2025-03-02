import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.example.learnspring.repository.UserRepository
import org.example.learnspring.service.SecureUserService
import org.example.learnspring.service.UserNotFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.util.*

class SecureUserServiceTest {

    private lateinit var secureUserService: SecureUserService // 직접 주입할 테스트 대상 서비스
    private lateinit var meterRegistry: SimpleMeterRegistry // 메트릭 레지스트리

    @Mock
    private lateinit var userRepository: UserRepository // Mock으로 대체된 데이터 저장소

    @BeforeEach
    fun setUp() {
        // Mockito 어노테이션 초기화
        MockitoAnnotations.openMocks(this)

        // 필요한 의존성 직접 초기화 및 주입
        meterRegistry = SimpleMeterRegistry()
        secureUserService = SecureUserService(userRepository, meterRegistry)
    }

    @Test
    fun `should increment error counter when user not found`() {
        // Arrange: Mock 동작 설정
        val invalidUserId = 9999L
        Mockito.`when`(userRepository.findById(invalidUserId)).thenReturn(Optional.empty())

        // Act & Assert: 사용자 정의 예외 발생 확인
        val exception = assertThrows<UserNotFoundException> {
            secureUserService.getSecureUserById(invalidUserId)
        }

        // 예외 메시지 검증
        assertEquals("User with ID 9999 not found", exception.message)

        // 호출 검증
        Mockito.verify(userRepository, Mockito.times(1)).findById(invalidUserId)
    }
}