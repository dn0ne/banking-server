package com.dn0ne.service

import com.dn0ne.model.user.User
import org.slf4j.LoggerFactory
import kotlin.random.Random

class VerificationService(
    private val mailService: MailService
) {
    private val logger = LoggerFactory.getLogger(VerificationService::class.java)

    private val pendingVerifications = mutableMapOf<String, User>()

    fun sendVerificationEmail(user: User) {
        logger.info("Forming verification data for ${user.username}")
        val code = generateCode().takeIf { it > 0 }?.toString() ?: run {
            logger.warn("Failed to add verification for ${user.username}: pool is full")
            return
        }

        val existingVerificationCode = pendingVerifications.entries
            .find { it.value == user }?.key

        existingVerificationCode?.let {
            logger.info("Verification code already exists for ${user.username}, assigning new one")
            pendingVerifications.remove(it)
        }

        pendingVerifications[code] = user
        logger.info("Verification code for ${user.username}: $code")

        mailService.sendVerificationEmail(code, user)
    }

    fun verifyEmail(token: String): User? {
        return pendingVerifications.remove(token)
    }

    private fun generateCode(): Int {
        var newCode: Int
        do {
            newCode = Random.nextInt(100_000, 1_000_000)
        } while (newCode.toString() !in pendingVerifications.keys)

        return newCode
    }
}