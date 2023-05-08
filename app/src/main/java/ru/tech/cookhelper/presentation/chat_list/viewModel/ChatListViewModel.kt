package ru.tech.cookhelper.presentation.chat_list.viewModel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.tech.cookhelper.core.Action
import ru.tech.cookhelper.domain.model.Chat
import ru.tech.cookhelper.domain.model.FileData
import ru.tech.cookhelper.domain.model.Message
import ru.tech.cookhelper.domain.model.User
import ru.tech.cookhelper.domain.use_case.get_chat_list.GetChatListUseCase
import ru.tech.cookhelper.domain.use_case.get_user.GetUserUseCase
import ru.tech.cookhelper.domain.use_case.search_user.SearchUserUseCase
import ru.tech.cookhelper.presentation.chat_list.components.ChatListState
import ru.tech.cookhelper.presentation.chat_list.components.UserListState
import ru.tech.cookhelper.presentation.ui.utils.compose.StateUtils.update
import ru.tech.cookhelper.presentation.ui.utils.event.Event
import ru.tech.cookhelper.presentation.ui.utils.event.ViewModelEvents
import ru.tech.cookhelper.presentation.ui.utils.event.ViewModelEventsImpl
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    getUserUseCase: GetUserUseCase,
    private val getChatListUseCase: GetChatListUseCase,
    private val getSearchUserUseCase: SearchUserUseCase
) : ViewModel(), ViewModelEvents<Event> by ViewModelEventsImpl() {

    private val _user: MutableState<User?> = mutableStateOf(null)
    val user: User? by _user

    private val _chatListState: MutableState<ChatListState> = mutableStateOf(ChatListState())
    val chatListState: ChatListState by _chatListState

    private val _chatListSearchMode: MutableState<Boolean> = mutableStateOf(false)
    val chatListSearchMode: Boolean by _chatListSearchMode
    fun searchModeChange() { _chatListSearchMode.update { !_chatListSearchMode.value } }

    private val _searchUserListState: MutableState<UserListState> = mutableStateOf(UserListState())
    val serchedUserListState: UserListState by _searchUserListState

    fun searchStringChange(string: String) {
        getSearchUserUseCase(string, user?.token ?: "").onEach { action ->
            when (action) {
                is Action.Success ->
                    _searchUserListState.value = UserListState(
                        isLoaded = true,
                        userList = action.data ?: emptyList()
                    )
                is Action.Empty ->
                    _searchUserListState.value = UserListState(
                        isLoaded = false
                    )
                else -> _searchUserListState.value = _searchUserListState.value
            }
        }.launchIn(viewModelScope)
    }


    init {
        getUserUseCase().onEach {
            _user.update { it }
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            _chatListState.value = ChatListState(isLoading = true)
            delay(500)
            _chatListState.value = ChatListState(
                chatList = listOf(
                    Chat(
                        id = 1,
                        images = listOf(
                            FileData(
                                "http://192.168.43.51:8080/data/chat/testChatLogo.jpg",
                                "CwtL5.png"
                            )
                        ),
                        title = "Чат",
                        lastMessage = Message(
                            0,
                            "О чем речь?",
                            emptyList(),
                            0,
                            System.currentTimeMillis(),
                            user!!
                        ),
                        newMessagesCount = 1,
                        members = emptyList(),
                        messages = emptyList(),
                        creationTimestamp = System.currentTimeMillis()
                    )
                )
            )
        }
    }
}