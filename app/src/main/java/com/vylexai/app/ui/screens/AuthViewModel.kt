package com.vylexai.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vylexai.app.data.auth.AuthRepository
import com.vylexai.app.data.net.VylexException
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMode { Login, Register }

sealed interface AuthEvent {
    data object Authenticated : AuthEvent
}

data class AuthUiState(
    val mode: AuthMode = AuthMode.Login,
    val email: String = "",
    val password: String = "",
    val submitting: Boolean = false,
    val error: String? = null
) {
    val canSubmit: Boolean
        get() = !submitting && isEmailValid(email) && password.length >= PASSWORD_MIN

    companion object {
        const val PASSWORD_MIN = 8
    }
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    private val _events = MutableStateFlow<AuthEvent?>(null)
    val events: StateFlow<AuthEvent?> = _events.asStateFlow()

    val alreadyAuthenticated: Boolean get() = repository.isAuthenticated()

    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value.trim(), error = null) }
    }

    fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value, error = null) }
    }

    fun onModeToggle() {
        _state.update { it.copy(mode = it.mode.flip(), error = null) }
    }

    fun submit() {
        val current = _state.value
        if (!current.canSubmit) return
        _state.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            runCatching {
                when (current.mode) {
                    AuthMode.Login -> repository.login(current.email, current.password)
                    AuthMode.Register -> repository.register(current.email, current.password)
                }
            }
                .onSuccess {
                    _state.update { it.copy(submitting = false) }
                    _events.value = AuthEvent.Authenticated
                }
                .onFailure { t ->
                    _state.update {
                        it.copy(submitting = false, error = renderError(t, current.mode))
                    }
                }
        }
    }

    fun consumeEvent() {
        _events.value = null
    }

    private fun renderError(t: Throwable, mode: AuthMode): String = when (t) {
        is VylexException.BadRequest -> {
            if (mode == AuthMode.Register && t.message?.contains("email_taken", true) == true) {
                "That email is already registered."
            } else {
                "Couldn't ${if (mode == AuthMode.Login) "sign in" else "create your account"} — check the form."
            }
        }
        is VylexException.AuthExpired -> "Invalid email or password."
        is VylexException.Unavailable -> "Couldn't reach the VylexAI network. Try again in a moment."
        else -> t.message ?: "Unknown error"
    }

    private fun AuthMode.flip(): AuthMode =
        if (this == AuthMode.Login) AuthMode.Register else AuthMode.Login
}

private val EMAIL_RE = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

internal fun isEmailValid(email: String): Boolean = EMAIL_RE.matches(email)
