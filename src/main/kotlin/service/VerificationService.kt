package com.dn0ne.service

import com.dn0ne.model.user.User
import java.util.UUID

class VerificationService(
    private val mailService: MailService
) {
    private val pendingVerifications = mutableMapOf<String, User>()

    fun sendVerificationEmail(user: User) {
        val token = UUID.randomUUID().toString()

        val existingVerificationToken = pendingVerifications.entries
            .find { it.value == user }?.key

        existingVerificationToken?.let {
            pendingVerifications.remove(it)
        }

        pendingVerifications[token] = user

        mailService.sendVerificationEmail(token, user)
    }

    fun verifyEmail(token: String): User? {
        return pendingVerifications.remove(token)
    }
}