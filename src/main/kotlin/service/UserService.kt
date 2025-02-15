package com.dn0ne.service

import com.dn0ne.model.user.User
import com.dn0ne.repository.UserRepository
import org.slf4j.LoggerFactory

class UserService(
    private val userRepository: UserRepository,
) {
    private val logger = LoggerFactory.getLogger(UserService::class.java)

    suspend fun findByUsername(username: String): User? =
        userRepository.findByUsername(username)

    suspend fun register(user: User): User? {
        logger.info("Trying to register user ${user.username}")
        val foundUser = findByUsername(user.username)

        return if (foundUser == null) {
            if (userRepository.insert(user)) {
                logger.info("User ${user.username} registered successfully")
                user
            } else {
                logger.error("Failed to register user ${user.username}")
                null
            }
        } else {
            logger.info("User ${foundUser.username} already exists")
            null
        }
    }

    suspend fun verify(user: User): Boolean {
        val foundUser = findByUsername(user.username) ?: return false

        return userRepository.update(
            foundUser.copy(
                state = User.State.Active
            )
        ).also {
            if (it) {
                logger.info("User ${foundUser.username} verified successfully")
            } else {
                logger.error("Failed to verify user ${foundUser.username}")
            }
        }
    }
}