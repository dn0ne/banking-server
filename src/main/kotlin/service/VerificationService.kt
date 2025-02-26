package com.dn0ne.service

import com.dn0ne.model.user.User
import com.dn0ne.routing.request.VerificationRequest
import org.slf4j.LoggerFactory
import kotlin.random.Random

class VerificationService(
    private val mailService: MailService
) {
    private val logger = LoggerFactory.getLogger(VerificationService::class.java)

    private val pendingVerifications = mutableMapOf<User, String>()

    fun sendVerificationEmail(user: User) {
        logger.info("Forming verification data for ${user.username}")
        val code = Random.nextInt(0, 1_000_000).toString().padStart(6, '0')

        pendingVerifications[user] = code
        logger.info("Verification code for ${user.username}: $code")

        mailService.sendVerificationEmail(code, user)
    }

    fun verifyEmail(request: VerificationRequest): User? {
        val entry = pendingVerifications.entries.find {
            request.username == it.key.username
                    && request.code == it.value
        }

        if (entry == null) return null

        pendingVerifications.remove(entry.key)
        return entry.key
    }
}