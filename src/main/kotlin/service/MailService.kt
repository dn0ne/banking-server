package com.dn0ne.service

import com.dn0ne.model.user.User
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.mailer.MailerBuilder
import org.slf4j.LoggerFactory

class MailService {
    private val logger = LoggerFactory.getLogger(MailService::class.java)

    private val mailer = MailerBuilder
        .withSMTPServer(
            "smtp.gmail.com",
            587,
            "dev.dn0ne@gmail.com",
            "tuvh ttvf krnc tbay"
        )
        .buildMailer()

    fun sendVerificationEmail(token: String, user: User) {
        logger.info("Sending verification for ${user.username}")

        val verificationLink = "http://localhost:8080/verify/$token"
        val email = EmailBuilder.startingBlank()
            .from("dev.dn0ne@gmail.com")
            .to(user.username)
            .withSubject("Email verification")
            .withPlainText("Click the link to verify your account: $verificationLink")
            .buildEmail()

        mailer.sendMail(email).whenComplete { _, throwable ->
            throwable?.let {
                logger.error("Failed to send verification email to ${user.username}", throwable.message)
            }
        }
    }
}