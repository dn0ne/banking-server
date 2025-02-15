package com.dn0ne.service

import com.dn0ne.model.user.User
import org.slf4j.LoggerFactory
import java.util.UUID

class VerificationService(
    private val mailService: MailService
) {
    private val logger = LoggerFactory.getLogger(VerificationService::class.java)

    private val pendingVerifications = mutableMapOf<String, User>()

    fun sendVerificationEmail(user: User) {
        logger.info("Forming verification data for ${user.username}")
        val token = UUID.randomUUID().toString()

        val existingVerificationToken = pendingVerifications.entries
            .find { it.value == user }?.key

        existingVerificationToken?.let {
            logger.info("Verification token already exists for ${user.username}, assigning new one")
            pendingVerifications.remove(it)
        }

        pendingVerifications[token] = user
        logger.info("Verification token for ${user.username}: $token")

        mailService.sendVerificationEmail(token, user)
    }

    fun verifyEmail(token: String): User? {
        return pendingVerifications.remove(token)
    }
}