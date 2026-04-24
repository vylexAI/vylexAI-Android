package com.vylexai.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vylexai.app.ui.components.BrandMark
import com.vylexai.app.ui.components.GhostButton
import com.vylexai.app.ui.components.PrimaryButton
import com.vylexai.app.ui.components.VylexBackdrop
import com.vylexai.app.ui.theme.VylexPalette
import com.vylexai.app.ui.theme.VylexTheme

@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    onSkipDemo: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        if (viewModel.alreadyAuthenticated) onAuthenticated()
    }
    LaunchedEffect(event) {
        if (event is AuthEvent.Authenticated) {
            viewModel.consumeEvent()
            onAuthenticated()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.Start
    ) {
        BrandMark(size = 80.dp)
        Spacer(Modifier.height(24.dp))
        Text(
            text = when (state.mode) {
                AuthMode.Login -> "Welcome back"
                AuthMode.Register -> "Create your account"
            },
            style = MaterialTheme.typography.displaySmall,
            color = VylexPalette.Text100
        )
        Text(
            text = when (state.mode) {
                AuthMode.Login -> "Sign in to your VylexAI node."
                AuthMode.Register -> "One account across every VylexAI device."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = VylexPalette.Text500,
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(Modifier.height(28.dp))

        VylexField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = "Email",
            keyboardType = KeyboardType.Email
        )
        Spacer(Modifier.height(12.dp))
        VylexField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = "Password",
            keyboardType = KeyboardType.Password,
            isPassword = true
        )
        state.error?.let {
            Spacer(Modifier.height(10.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = VylexPalette.Rose400
            )
        }

        Spacer(Modifier.height(24.dp))
        if (state.submitting) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = VylexPalette.Cyan300, strokeWidth = 2.dp)
            }
        } else {
            PrimaryButton(
                text = when (state.mode) {
                    AuthMode.Login -> "Sign in"
                    AuthMode.Register -> "Create account"
                },
                onClick = viewModel::submit,
                enabled = state.canSubmit
            )
        }
        Spacer(Modifier.height(12.dp))
        GhostButton(
            text = when (state.mode) {
                AuthMode.Login -> "Need an account? Register"
                AuthMode.Register -> "Already have one? Sign in"
            },
            onClick = viewModel::onModeToggle
        )

        Spacer(Modifier.height(24.dp))
        TextButton(
            onClick = onSkipDemo,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Continue as demo — no data leaves the phone",
                style = MaterialTheme.typography.labelMedium,
                color = VylexPalette.Text500
            )
        }
    }
}

@Composable
private fun VylexField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = VylexPalette.Text100,
            unfocusedTextColor = VylexPalette.Text100,
            focusedContainerColor = VylexPalette.Ink700,
            unfocusedContainerColor = VylexPalette.Ink700,
            focusedBorderColor = VylexPalette.Cyan400,
            unfocusedBorderColor = VylexPalette.Ink500,
            focusedLabelColor = VylexPalette.Cyan300,
            unfocusedLabelColor = VylexPalette.Text500
        )
    )
}

@Preview
@Composable
private fun AuthPreview() {
    VylexTheme { VylexBackdrop { /* Hilt VM needed — use Studio preview */ } }
}
