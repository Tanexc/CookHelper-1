package ru.tech.cookhelper.presentation.chat_list.components

import ru.tech.cookhelper.domain.model.User

data class UserListState(
    val isLoaded: Boolean = false,
    val userList: List<User> = emptyList()
)