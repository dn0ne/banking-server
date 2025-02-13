package com.dn0ne.repository

import io.github.cdimascio.dotenv.dotenv
import org.jetbrains.exposed.sql.Database
import org.postgresql.ds.PGSimpleDataSource

internal val dotenv = dotenv {
    directory = "./database/"
    filename = "postgres.env"
}

internal val dbUser = dotenv.get("POSTGRES_USER")
    ?: throw IllegalArgumentException("Missing POSTGRES_USER environment variable")

internal val dbPassword = dotenv.get("POSTGRES_PASSWORD")
    ?: throw IllegalArgumentException("Missing POSTGRES_PASSWORD environment variable")

internal val dbName = dotenv.get("POSTGRES_DB")
    ?: throw IllegalArgumentException("Missing POSTGRES_DB environment variable")

internal val dataSource = PGSimpleDataSource().apply {
    setURL("jdbc:postgresql://localhost:5433/postgres")
    user = dbUser
    password = dbPassword
    databaseName = dbName
}
internal val database = Database.connect(dataSource)