package ru.tech.cookhelper.presentation.restore_password.viewModel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Password
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.tech.cookhelper.R
import ru.tech.cookhelper.core.Action
import ru.tech.cookhelper.domain.use_case.restore_password.ApplyPasswordByCodeUseCase
import ru.tech.cookhelper.domain.use_case.restore_password.SendRestoreCodeUseCase
import ru.tech.cookhelper.presentation.authentication.components.getMessage
import ru.tech.cookhelper.presentation.confirm_email.components.CodeState
import ru.tech.cookhelper.presentation.restore_password.components.RestorePasswordState
import ru.tech.cookhelper.presentation.restore_password.components.RestoreState
import ru.tech.cookhelper.presentation.ui.utils.compose.UIText
import ru.tech.cookhelper.presentation.ui.utils.event.Event
import ru.tech.cookhelper.presentation.ui.utils.event.ViewModelEvents
import ru.tech.cookhelper.presentation.ui.utils.event.ViewModelEventsImpl
import ru.tech.cookhelper.presentation.ui.utils.navigation.Screen
import javax.inject.Inject

@HiltViewModel
class RestorePasswordViewModel @Inject constructor(
    private val sendRestoreCodeUseCase: SendRestoreCodeUseCase,
    private val applyPasswordByCodeUseCase: ApplyPasswordByCodeUseCase,
) : ViewModel(), ViewModelEvents<Event> by ViewModelEventsImpl() {

    private var restoreLogin = ""

    private val _restorePasswordState: MutableState<RestorePasswordState> =
        mutableStateOf(RestorePasswordState())
    val restorePasswordState: RestorePasswordState by _restorePasswordState

    private val _restorePasswordCodeState: MutableState<CodeState> = mutableStateOf(CodeState())
    val restorePasswordCodeState: CodeState by _restorePasswordCodeState

    fun restorePasswordBy(login: String) {
        viewModelScope.launch {
            _restorePasswordState.value = _restorePasswordState.value.copy(isLoading = true)
            _restorePasswordCodeState.value = CodeState(isLoading = true)
            when (val result = sendRestoreCodeUseCase(login)) {
                is Action.Empty -> {
                    sendEvent(
                        Event.ShowToast(
                            UIText.StringResource(result.status.getMessage()),
                            Icons.Rounded.ErrorOutline
                        )
                    )
                    _restorePasswordCodeState.value = CodeState(error = true)
                    _restorePasswordState.value =
                        _restorePasswordState.value.copy(isLoading = false)
                }
                is Action.Error -> {
                    _restorePasswordCodeState.value = CodeState(error = true)
                    _restorePasswordState.value =
                        _restorePasswordState.value.copy(isLoading = false)
                    sendEvent(
                        Event.ShowToast(
                            UIText.DynamicString(result.message.toString()),
                            Icons.Rounded.ErrorOutline
                        )
                    )
                }
                is Action.Success -> {
                    _restorePasswordState.value =
                        RestorePasswordState(state = RestoreState.Password)
                    _restorePasswordCodeState.value = CodeState()
                    restoreLogin = login
                }
                else -> {}
            }
        }
    }

    fun applyPasswordByCode(code: String, password: String) {
        applyPasswordByCodeUseCase(restoreLogin, code, password).onEach { result ->
            when (result) {
                is Action.Loading -> _restorePasswordState.value =
                    RestorePasswordState(
                        isLoading = true,
                        state = RestoreState.Password
                    )
                is Action.Error -> {
                    _restorePasswordState.value =
                        RestorePasswordState(state = RestoreState.Password)
                    sendEvent(
                        Event.ShowToast(
                            UIText.DynamicString(result.message.toString()),
                            Icons.Outlined.ErrorOutline
                        )
                    )
                }
                is Action.Success -> {
                    result.data?.let {
                        _restorePasswordState.value = RestorePasswordState(
                            user = it,
                            state = RestoreState.Password
                        )
                        sendEvent(Event.NavigateTo(Screen.Authentication.Login))
                        sendEvent(
                            Event.ShowToast(
                                UIText.StringResource(R.string.password_changed),
                                Icons.Rounded.Password
                            )
                        )
                    }
                }
                is Action.Empty -> {
                    _restorePasswordState.value =
                        RestorePasswordState(state = RestoreState.Password)
                    sendEvent(
                        Event.ShowToast(
                            UIText.StringResource(R.string.wrong_code),
                            Icons.Outlined.ErrorOutline
                        )
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun goBackPasswordRestore() {
        _restorePasswordState.value = _restorePasswordState.value.copy(state = RestoreState.Login)
    }

}