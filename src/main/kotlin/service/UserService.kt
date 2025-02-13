package com.dn0ne.service

import com.dn0ne.model.user.User
import com.dn0ne.repository.UserRepository

class UserService(
    private val userRepository: UserRepository,
) {
    suspend fun findByUsername(username: String): User? =
        userRepository.findByUsername(username)

    suspend fun register(user: User): User? {
        val foundUser = findByUsername(user.username)

        return if (foundUser == null) {
            userRepository.insert(user)
            user
        } else null
    }

    suspend fun verify(user: User): Boolean {
        val foundUser = findByUsername(user.username) ?: return false

        return userRepository.update(
            foundUser.copy(
                state = User.State.Active
            )
        )
    }
}