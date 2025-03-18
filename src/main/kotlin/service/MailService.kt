package com.dn0ne.service

import com.dn0ne.model.user.User
import io.github.cdimascio.dotenv.dotenv
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.mailer.MailerBuilder
import org.slf4j.LoggerFactory

private val dotenv = dotenv {
    directory = "./"
    filename = "mail.env"
}

private val mailHost = dotenv.get("MAIL_HOST")
    ?: throw IllegalArgumentException("Missing MAIL_HOST environment variable")

private val mailPort = dotenv.get("MAIL_PORT")?.toIntOrNull()
    ?: throw IllegalArgumentException("Missing MAIL_PORT environment variable")

private val mailUsername = dotenv.get("MAIL_USERNAME")
    ?: throw IllegalArgumentException("Missing MAIL_USERNAME environment variable")

private val mailPassword = dotenv.get("MAIL_PASSWORD")
    ?: throw IllegalArgumentException("Missing MAIL_PASSWORD environment variable")

class MailService {
    private val logger = LoggerFactory.getLogger(MailService::class.java)

    private val mailer = MailerBuilder
        .withSMTPServer(
            mailHost,
            mailPort,
            mailUsername,
            mailPassword
        )
        .buildMailer()

    fun sendVerificationEmail(code: String, user: User) {
        logger.info("Sending verification for ${user.username}")

        val email = EmailBuilder.startingBlank()
            .from("dev.dn0ne@gmail.com")
            .to(user.username)
            .withSubject("Email verification")
            .withPlainText("Your verification code is $code")
            .buildEmail()

        mailer.sendMail(email).whenComplete { _, throwable ->
            throwable?.let {
                logger.error("Failed to send verification email to ${user.username}", throwable.message)
            }
        }
    }
}