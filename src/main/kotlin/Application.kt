package com.dn0ne

import com.dn0ne.plugins.configureDatabase
import com.dn0ne.plugins.configureSecurity
import com.dn0ne.plugins.configureSerialization
import com.dn0ne.repository.UserRepository
import com.dn0ne.routing.configureRouting
import com.dn0ne.service.JwtService
import com.dn0ne.service.MailService
import com.dn0ne.service.UserService
import com.dn0ne.service.VerificationService
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val userRepository = UserRepository()
    val userService = UserService(userRepository)
    val jwtService = JwtService(this, userService)

    val mailService = MailService()
    val verificationService = VerificationService(mailService)

    configureSerialization()
    configureSecurity(jwtService)
    configureDatabase()
    configureRouting(userService, jwtService, verificationService)
}
