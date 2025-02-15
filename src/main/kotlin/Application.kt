package com.dn0ne

import com.dn0ne.plugins.configureDatabase
import com.dn0ne.plugins.configureSecurity
import com.dn0ne.plugins.configureSerialization
import com.dn0ne.repository.AccountRepository
import com.dn0ne.repository.TransactionRepository
import com.dn0ne.repository.UserRepository
import com.dn0ne.routing.configureRouting
import com.dn0ne.service.*
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

    val accountRepository = AccountRepository()
    val transactionRepository = TransactionRepository()
    val accountService = AccountService(
        userRepository,
        accountRepository,
        transactionRepository
    )
    val transactionService = TransactionService(
        transactionRepository,
        accountService
    )

    configureSerialization()
    configureSecurity(jwtService)
    configureDatabase()
    configureRouting(
        userService,
        jwtService,
        verificationService,
        accountService,
        transactionService
    )
}
