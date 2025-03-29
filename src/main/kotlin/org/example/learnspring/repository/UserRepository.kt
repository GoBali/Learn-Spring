package org.example.learnspring.repository

import org.example.learnspring.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): Optional<User>
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.deleted = TRUE WHERE u.email = :email")
    fun softDeleteByEmail(@Param("email") email: String): Int
    fun findAllByDeletedFalse(): List<User>
}