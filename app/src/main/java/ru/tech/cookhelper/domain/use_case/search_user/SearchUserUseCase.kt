package ru.tech.cookhelper.domain.use_case.search_user

import android.provider.ContactsContract.CommonDataKinds.Nickname
import ru.tech.cookhelper.domain.repository.UserRepository
import javax.inject.Inject

class SearchUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(stringUserData: String, token: String) = userRepository.searchUser(stringUserData, token)
}