package com.dn0ne.repository

import com.dn0ne.model.user.User
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

class UserRepositoryTest {

    private val userRepository = UserRepository()

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
    fun `add user to repository`() = runTest {
        expectThat(userRepository.insert(user1)).isTrue()
        expectThat(userRepository.insert(user2)).isTrue()

        expectThat(userRepository.insert(user1)).isFalse()
    }

    @Test
    fun `find user in repository by id`() = runTest {
        userRepository.insert(user1)
        userRepository.insert(user2)

        expectThat(userRepository.findById(user1.id)).isEqualTo(user1)
        expectThat(userRepository.findById(nonExistingUser.id)).isNull()
    }

    @Test
    fun `find user in repository by username`() = runTest {
        userRepository.insert(user1)
        userRepository.insert(user2)

        expectThat(userRepository.findByUsername(user1.username)).isEqualTo(user1)
        expectThat(userRepository.findByUsername(nonExistingUser.username)).isNull()
    }
}