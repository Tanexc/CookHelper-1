package ru.tech.cookhelper.presentation.post_creation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import dev.olshevski.navigation.reimagined.hilt.hiltViewModel
import ru.tech.cookhelper.R
import ru.tech.cookhelper.domain.model.Post
import ru.tech.cookhelper.domain.model.getLastAvatar
import ru.tech.cookhelper.presentation.post_creation.viewModel.PostCreationViewModel
import ru.tech.cookhelper.presentation.recipe_post_creation.components.LeaveUnsavedDataDialog
import ru.tech.cookhelper.presentation.ui.utils.android.ContextUtils.getFile
import ru.tech.cookhelper.presentation.ui.utils.compose.UIText.Companion.UIText
import ru.tech.cookhelper.presentation.ui.utils.compose.show
import ru.tech.cookhelper.presentation.ui.utils.compose.widgets.Picture
import ru.tech.cookhelper.presentation.ui.utils.event.Event
import ru.tech.cookhelper.presentation.ui.utils.event.collectWithLifecycle
import ru.tech.cookhelper.presentation.ui.utils.provider.LocalScreenController
import ru.tech.cookhelper.presentation.ui.utils.provider.LocalToastHostState
import ru.tech.cookhelper.presentation.ui.utils.provider.goBack
import ru.tech.cookhelper.presentation.ui.widgets.CozyTextField
import ru.tech.cookhelper.presentation.ui.widgets.LoadingDialog
import ru.tech.cookhelper.presentation.ui.widgets.TextFieldAppearance
import ru.tech.cookhelper.presentation.ui.widgets.TopAppBar


@OptIn(ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class)
@Composable
fun PostCreationScreen(
    viewModel: PostCreationViewModel = hiltViewModel(),
    initialImageUri: String = "",
    /*TODO: Remove this shit*/ todoRemoveThisFuckingCostyl: (Post?) -> Unit
) {
    val controller = LocalScreenController.current
    val onBack: () -> Unit = { controller.goBack() }

    val context = LocalContext.current
    val toastHost = LocalToastHostState.current
    val focus = LocalFocusManager.current
    var doneEnabled by rememberSaveable { mutableStateOf(false) }

    var content by rememberSaveable { mutableStateOf("") }
    var label by rememberSaveable { mutableStateOf("") }
    var imageUri by rememberSaveable { mutableStateOf(initialImageUri) }

    val user = viewModel.user

    var showLeaveUnsavedDataDialog by rememberSaveable { mutableStateOf(false) }

    val goBack = {
        if (imageUri.isNotEmpty() || content.isNotEmpty() || label.isNotEmpty()) {
            showLeaveUnsavedDataDialog = true
        } else onBack()
    }

    Column(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focus.clearFocus() })
            }
    ) {
        TopAppBar(
            background = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .fillMaxWidth()
                ) {
                    Picture(model = user?.getLastAvatar(), modifier = Modifier.size(40.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "${user?.name?.trim()} ${user?.surname?.trim()}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { goBack() }) {
                    Icon(Icons.Rounded.Close, null)
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        val file = context.getFile(imageUri)
                        val type = context.contentResolver.getType(imageUri.toUri())
                        viewModel.createPost(content, label, file, type ?: "")
                    },
                    enabled = doneEnabled,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Rounded.Done, null)
                }
            }
        )

        val paddingValues = PaddingValues(start = 8.dp, end = 8.dp, bottom = 16.dp)

        val resultLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = {
                it?.let { uri ->
                    imageUri = uri.toString()
                }
            }
        )

        val pickImage = {
            resultLauncher.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }

        LazyColumn(
            Modifier
                .fillMaxSize()
                .imePadding(),
            contentPadding = WindowInsets.navigationBars.asPaddingValues(),
        ) {
            item {
                CozyTextField(
                    value = label,
                    appearance = TextFieldAppearance.Rounded,
                    onValueChange = { label = it },
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp, top = 16.dp)
                        .fillMaxWidth(),
                    label = {
                        Text(
                            stringResource(R.string.enter_headline),
                            modifier = Modifier.offset(y = 4.dp)
                        )
                    },
                    textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                CozyTextField(
                    value = content,
                    appearance = TextFieldAppearance.Rounded,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 32.dp)
                        .fillMaxWidth(),
                    label = {
                        Text(
                            stringResource(R.string.whats_new),
                            modifier = Modifier.offset(y = 4.dp)
                        )
                    },
                    textStyle = TextStyle(fontSize = 20.sp)
                )

                AnimatedContent(targetState = imageUri) { uri ->
                    if (uri.isEmpty()) {
                        OutlinedButton(
                            modifier = Modifier
                                .padding(paddingValues)
                                .fillMaxWidth(),
                            onClick = pickImage
                        ) {
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Outlined.Image, null)
                            Spacer(Modifier.width(16.dp))
                            Text(stringResource(R.string.add_image))
                            Spacer(Modifier.width(8.dp))
                        }
                    } else {
                        Box {
                            Picture(
                                model = imageUri,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(paddingValues),
                                shape = RoundedCornerShape(24.dp)
                            )
                            FilledIconButton(
                                modifier = Modifier
                                    .padding(end = 14.dp, top = 6.dp)
                                    .size(40.dp)
                                    .align(Alignment.TopEnd),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                ),
                                onClick = { imageUri = "" }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Cancel,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
            }
        }

        LaunchedEffect(imageUri, content) {
            doneEnabled = imageUri.isNotEmpty() || content.isNotEmpty()
        }

        LoadingDialog(
            visible = viewModel.postCreationState.isLoading,
            isCancelable = true,
            onDismiss = {

            }
        )

        if (viewModel.postCreationState.post != null) {
            LaunchedEffect(Unit) {
                toastHost.show(
                    Icons.Rounded.Done, UIText(R.string.post_created).asString(context)
                )
                onBack()
                todoRemoveThisFuckingCostyl(viewModel.postCreationState.post)
            }

        }

    }

    BackHandler { goBack() }

    viewModel.eventFlow.collectWithLifecycle {
        when (it) {
            is Event.ShowToast -> toastHost.show(
                it.icon,
                it.text.asString(context)
            )
            else -> {}
        }
    }

    if (showLeaveUnsavedDataDialog) {
        LeaveUnsavedDataDialog(
            title = R.string.post_creation_started,
            message = R.string.post_creation_started_leave_message,
            onLeave = { onBack() },
            onDismissRequest = { showLeaveUnsavedDataDialog = false }
        )
    }
}