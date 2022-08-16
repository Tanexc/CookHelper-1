package ru.tech.cookhelper.presentation.authentication.components.login

import ru.tech.cookhelper.domain.model.User

data class LoginState(
    val isLoading: Boolean = false,
    val user: User? = null
)