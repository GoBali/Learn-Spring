package org.example.learnspring.repository

import org.example.learnspring.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long>