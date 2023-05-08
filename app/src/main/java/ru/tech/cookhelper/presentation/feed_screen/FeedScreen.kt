package ru.tech.cookhelper.presentation.feed_screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WifiTetheringOff
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.olshevski.navigation.reimagined.hilt.hiltViewModel
import ru.tech.cookhelper.R
import ru.tech.cookhelper.presentation.feed_screen.viewModel.FeedViewModel
import ru.tech.cookhelper.presentation.profile.components.ProfileRecipeItem
import ru.tech.cookhelper.presentation.ui.utils.compose.show
import ru.tech.cookhelper.presentation.ui.utils.event.Event
import ru.tech.cookhelper.presentation.ui.utils.event.collectWithLifecycle
import ru.tech.cookhelper.presentation.ui.utils.navigation.Screen
import ru.tech.cookhelper.presentation.ui.utils.provider.LocalScreenController
import ru.tech.cookhelper.presentation.ui.utils.provider.LocalToastHostState
import ru.tech.cookhelper.presentation.ui.utils.provider.navigate
import ru.tech.cookhelper.presentation.ui.widgets.Loading
import ru.tech.cookhelper.presentation.ui.widgets.Placeholder

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FeedScreen(viewModel: FeedViewModel = hiltViewModel()) {
    val screenController = LocalScreenController.current
    val lazyListState = rememberLazyListState()

    LaunchedEffect(viewModel.feedState.data) {
        if (lazyListState.firstVisibleItemScrollOffset == 0) {
            lazyListState.animateScrollToItem(0)
        }
    }

    if (viewModel.feedState.data.isNotEmpty()) {
        LazyColumn(contentPadding = PaddingValues(top = 8.dp), state = lazyListState) {
            items(
                items = viewModel.feedState.data,
            ) { item ->
                ProfileRecipeItem(
                    currentUser = viewModel.user.user,
                    recipePost = item,
                    onRecipeClick = {
                        screenController.navigate(Screen.RecipeDetails(item))
                    },
                    onAuthorClick = {
                        //TODO: Open Author page
                    }
                )
            }
        }
    } else {
        Loading()
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