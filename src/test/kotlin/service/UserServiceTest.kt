package com.dn0ne.service

import com.dn0ne.model.user.User
import com.dn0ne.repository.UserRepository
import com.dn0ne.repository.database
import com.dn0ne.repository.table.UsersTable
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNull
import strikt.assertions.isTrue
import java.util.*

class UserServiceTest {

    private val userRepository = UserRepository()
    private val userService: UserService = UserService(userRepository)

    private val user1 = User(
        id = UUID.randomUUID(),
        username = "test1",
        password = "test1",
        state = User.State.VerificationRequired
    )
    private val user2 = User(
        id = UUID.randomUUID(),
        username = "test2",
        password = "test2",
        state = User.State.VerificationRequired
    )
    private val nonExistingUser = User(
        id = UUID.randomUUID(),
        username = "not-exist",
        password = "not-exist",
        state = User.State.VerificationRequired
    )

    @BeforeEach
    fun resetDatabase() {
        transaction(database) {
            SchemaUtils.drop(UsersTable)
            SchemaUtils.create(UsersTable)
        }
    }

    @Test
    fun `register new user`() = runTest {
        expectThat(userService.register(user1)).isEqualTo(user1)
        expectThat(userService.register(user2)).isEqualTo(user2)

        expectThat(userService.register(user1)).isNull()
        expectThat(userService.register(user1.copy(id = UUID.randomUUID()))).isNull()
    }

    @Test
    fun `find user by username`() = runTest {
        userService.register(user1)
        userService.register(user2)

        expectThat(userService.findByUsername(user1.username))
            .isEqualTo(user1)

        expectThat(userService.findByUsername(nonExistingUser.username))
            .isNull()
    }

    @Test
    fun `verify user`() = runTest {
        userService.register(user1)

        expectThat(userService.verify(user1)).isTrue()
        expectThat(userService.verify(nonExistingUser)).isFalse()

        expectThat(userService.findByUsername(user1.username))
            .isEqualTo(user1.copy(state = User.State.Active))
    }
}