package ru.sonso.exception

class UserNotFoundException(
    message: String = "User not found",
    e: Throwable? = null
) : Exception(message, e)
