package com.dn0ne.util

import at.favre.lib.crypto.bcrypt.BCrypt

fun String.hash(): String {
    return BCrypt.withDefaults().hashToString(12, this.toCharArray())
}

fun String.verify(hashedPassword: String): Boolean {
    return BCrypt.verifyer().verify(
        this.toCharArray(),
        hashedPassword.toCharArray()
    ).verified
}