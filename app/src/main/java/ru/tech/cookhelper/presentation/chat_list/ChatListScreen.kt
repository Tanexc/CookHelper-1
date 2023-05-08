package ru.tech.cookhelper.presentation.chat_list

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import dev.olshevski.navigation.reimagined.hilt.hiltViewModel
import ru.tech.cookhelper.R
import ru.tech.cookhelper.presentation.chat_list.components.ChatListItem
import ru.tech.cookhelper.presentation.chat_list.viewModel.ChatListViewModel
import ru.tech.cookhelper.presentation.recipe_post_creation.components.ExpandableFloatingActionButton
import ru.tech.cookhelper.presentation.recipe_post_creation.components.Separator
import ru.tech.cookhelper.presentation.ui.theme.CreateAlt
import ru.tech.cookhelper.presentation.ui.theme.MessageDraw
import ru.tech.cookhelper.presentation.ui.utils.compose.PaddingUtils.addPadding
import ru.tech.cookhelper.presentation.ui.utils.compose.ScrollUtils.isScrollingUp
import ru.tech.cookhelper.presentation.ui.utils.compose.TopAppBarUtils
import ru.tech.cookhelper.presentation.ui.utils.compose.show
import ru.tech.cookhelper.presentation.ui.utils.event.Event
import ru.tech.cookhelper.presentation.ui.utils.event.collectWithLifecycle
import ru.tech.cookhelper.presentation.ui.utils.navigation.Screen
import ru.tech.cookhelper.presentation.ui.utils.provider.LocalScreenController
import ru.tech.cookhelper.presentation.ui.utils.provider.LocalToastHostState
import ru.tech.cookhelper.presentation.ui.utils.provider.navigate
import ru.tech.cookhelper.presentation.ui.widgets.Loading
import ru.tech.cookhelper.presentation.ui.widgets.Placeholder
import ru.tech.cookhelper.presentation.ui.widgets.TopAppBar

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(viewModel: ChatListViewModel = hiltViewModel()) {
    val screenController = LocalScreenController.current
    val chatListState = viewModel.chatListState
    val searchedUserListState = viewModel.serchedUserListState
    val lazyListState = rememberLazyListState()
    val searchMode: Boolean = viewModel.chatListSearchMode

    val scrollBehavior = TopAppBarUtils.topAppBarScrollBehavior()

    val searchUserModeSwitch: () -> Unit = { viewModel.searchModeChange() }
    val searchStringChange: (String) -> Unit = { string -> viewModel.searchStringChange(string) }

    Box {
        Column(
            Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .fillMaxWidth()
                    ) {
                        TextField(
                            TextFieldValue(""),
                            onValueChange = { string -> searchStringChange(string.text) }
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { searchUserModeSwitch() }) {
                        when (searchMode) {
                            true -> Icon(Icons.Rounded.Search, null)
                            else -> Icon(Icons.Rounded.Close, null)
                        }
                    }
                },
            )
            when (searchMode) {
                true ->
                    AnimatedContent(
                        targetState = searchedUserListState,
                        modifier = Modifier.fillMaxSize()
                    ) { state ->
                        when (state.isLoaded) {
                            true ->
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    state = lazyListState,
                                    contentPadding = WindowInsets.navigationBars.asPaddingValues()
                                        .addPadding(bottom = 80.dp)
                                ) {
                                    itemsIndexed(
                                        state.userList,
                                        key = {_, item -> item.id}
                                    ) { index, user ->



                                    }
                                }
                            else -> Loading()
                        }
                    }
                else ->
                    AnimatedContent(
                        targetState = chatListState,
                        modifier = Modifier.fillMaxSize()
                    ) { state ->
                        if (state.chatList.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                state = lazyListState,
                                contentPadding = WindowInsets.navigationBars.asPaddingValues()
                                    .addPadding(bottom = 80.dp)
                            ) {
                                itemsIndexed(
                                    state.chatList,
                                    key = { _, item -> item.id }) { index, chat ->
                                    ChatListItem(
                                        onClick = {
                                            screenController.navigate(
                                                Screen.Chat(
                                                    chatId = chat.id,
                                                    chatTitle = chat.title,
                                                    imageUrl = chat.images?.lastOrNull()?.link
                                                )
                                            )
                                        },
                                        image = chat.images?.lastOrNull()?.link,
                                        title = chat.title,
                                        lastMessageText = chat.lastMessage?.text ?: "",
                                        lastMessageTimestamp = chat.lastMessage?.timestamp ?: 0L,
                                        newMessagesCount = chat.newMessagesCount
                                    )
                                    if (index != state.chatList.lastIndex) Separator()
                                }
                            }
                        } else if (!state.isLoading) {
                            Placeholder(
                                icon = Icons.Filled.MessageDraw,
                                text = stringResource(R.string.no_existing_chats)
                            )
                        } else {
                            Loading()
                        }
                    }
            }

        }
        if (!searchMode) {
            ExpandableFloatingActionButton(
                onClick = { /*TODO*/ },
                expanded = lazyListState.isScrollingUp(),
                icon = {
                    Icon(Icons.Rounded.CreateAlt, null)
                },
                text = { Text(stringResource(R.string.new_chat)) },
                modifier = Modifier
                    .padding(16.dp)
                    .navigationBarsPadding()
                    .align(Alignment.BottomEnd)
            )
        }

    }

    val toastHost = LocalToastHostState.current
    val context = LocalContext.current
    viewModel.eventFlow.collectWithLifecycle {
        when (it) {
            is Event.ShowToast -> toastHost.show(
                Icons.Rounded.ErrorOutline,
                it.text.asString(context)
            )
            else -> {}
        }
    }

}