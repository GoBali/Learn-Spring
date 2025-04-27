package org.example.learnspring.utility

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class EncryptionUtilityTest {
    
    private lateinit var encryptionUtility: EncryptionUtility
    
    @BeforeEach
    fun setUp() {
        // Create the utility with a test secret key
        val testSecretKey = "ThisIsATestSecretKey1234"
        encryptionUtility = EncryptionUtility(testSecretKey)
    }
    
    @Test
    fun `encrypt and decrypt should return original string`() {
        // Given
        val originalText = "This is a test message"
        
        // When
        val encrypted = encryptionUtility.encrypt(originalText)
        val decrypted = encryptionUtility.decrypt(encrypted)
        
        // Then
        assertNotEquals(originalText, encrypted, "Encrypted text should be different from original")
        assertEquals(originalText, decrypted, "Decrypted text should match original")
    }
    
    @Test
    fun `encrypt should produce different output for same input on different instances`() {
        // Given
        val text = "Same input text"
        val anotherSecretKey = "AnotherTestSecretKey1234"
        val anotherEncryptionUtility = EncryptionUtility(anotherSecretKey)
        
        // When
        val encrypted1 = encryptionUtility.encrypt(text)
        val encrypted2 = anotherEncryptionUtility.encrypt(text)
        
        // Then
        assertNotEquals(encrypted1, encrypted2, "Encryption with different keys should produce different results")
    }
    
    @Test
    fun `hash should produce consistent output for same input`() {
        // Given
        val input = "Text to hash"
        
        // When
        val hash1 = encryptionUtility.hash(input)
        val hash2 = encryptionUtility.hash(input)
        
        // Then
        assertEquals(hash1, hash2, "Hash should be consistent for the same input")
        assertTrue(hash1.matches(Regex("[0-9a-f]+")), "Hash should be a hexadecimal string")
    }
    
    @Test
    fun `hash should produce different output for different inputs`() {
        // Given
        val input1 = "First text"
        val input2 = "Second text"
        
        // When
        val hash1 = encryptionUtility.hash(input1)
        val hash2 = encryptionUtility.hash(input2)
        
        // Then
        assertNotEquals(hash1, hash2, "Different inputs should produce different hashes")
    }
}