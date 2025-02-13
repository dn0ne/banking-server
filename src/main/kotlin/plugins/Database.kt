package com.dn0ne.plugins

import com.dn0ne.repository.table.UsersTable
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.postgresql.ds.PGSimpleDataSource

private val dotenv = dotenv {
    directory = "./database/"
    filename = "postgres.env"
}

private val dbUser = dotenv.get("POSTGRES_USER")
    ?: throw IllegalArgumentException("Missing POSTGRES_USER environment variable")

private val dbPassword = dotenv.get("POSTGRES_PASSWORD")
    ?: throw IllegalArgumentException("Missing POSTGRES_PASSWORD environment variable")

private val dbName = dotenv.get("POSTGRES_DB")
    ?: throw IllegalArgumentException("Missing POSTGRES_DB environment variable")

fun Application.configureDatabase() {
    val dataSource = PGSimpleDataSource().apply {
        user = dbUser
        password = dbPassword
        databaseName = dbName
    }

    val database = Database.connect(dataSource)

    transaction(database) {
        SchemaUtils.drop(UsersTable)
        SchemaUtils.create(UsersTable)
    }
}