package com.company.selvabooking.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.company.selvabooking.ui.components.ErrorMessage
import com.company.selvabooking.ui.components.LoadingIndicator
import com.company.selvabooking.ui.components.LogoDisplaySize
import com.company.selvabooking.ui.components.SelvaButton
import com.company.selvabooking.ui.components.SelvaLogo
import com.company.selvabooking.ui.components.SelvaScaffold
import com.company.selvabooking.ui.components.SelvaTextField
import com.company.selvabooking.ui.components.SelvaTopAppBar
import com.company.selvabooking.ui.components.SuccessMessage
import com.company.selvabooking.ui.theme.ForestGreen
import com.company.selvabooking.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isAuthenticated) {
        onLoginSuccess()
        return
    }

    SelvaScaffold { padding ->
        if (uiState.isLoading) {
            LoadingIndicator()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                SelvaLogo(size = LogoDisplaySize.Auth)
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Bienvenido",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Inicia sesión para continuar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(28.dp))

                SelvaTextField(
                    value = uiState.email,
                    onValueChange = viewModel::updateEmail,
                    label = "Correo electrónico",
                    keyboardType = KeyboardType.Email,
                    error = uiState.emailError
                )
                Spacer(modifier = Modifier.height(12.dp))
                SelvaTextField(
                    value = uiState.password,
                    onValueChange = viewModel::updatePassword,
                    label = "Contraseña",
                    isPassword = true,
                    error = uiState.passwordError
                )
                TextButton(
                    onClick = onNavigateToForgotPassword,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (uiState.error != null) {
                    ErrorMessage(uiState.error!!)
                }
                Spacer(modifier = Modifier.height(24.dp))
                SelvaButton(
                    text = "Iniciar sesión",
                    onClick = viewModel::login,
                    enabled = !uiState.isLoading
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onNavigateToRegister) {
                    Text("¿No tienes cuenta? Regístrate")
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    SelvaScaffold(
        topBar = {
            SelvaTopAppBar(
                title = "Recuperar contraseña",
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearResetState()
                        onNavigateBack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = ForestGreen
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isResetLoading) {
            LoadingIndicator(Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Restablecer contraseña",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ingresa tu correo electrónico y te enviaremos un enlace para restablecer tu contraseña.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(28.dp))

                SelvaTextField(
                    value = uiState.email,
                    onValueChange = viewModel::updateEmail,
                    label = "Correo electrónico",
                    keyboardType = KeyboardType.Email,
                    error = uiState.emailError
                )

                if (uiState.resetError != null) {
                    ErrorMessage(uiState.resetError!!)
                }
                if (uiState.isResetEmailSent) {
                    SuccessMessage(
                        "Se envió un correo a ${uiState.email.trim()}. Revisa tu bandeja de entrada y sigue las instrucciones."
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                SelvaButton(
                    text = if (uiState.isResetEmailSent) "Reenviar correo" else "Enviar enlace",
                    onClick = viewModel::sendPasswordResetEmail,
                    enabled = !uiState.isResetLoading
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = {
                    viewModel.clearResetState()
                    onNavigateBack()
                }) {
                    Text("Volver al inicio de sesión")
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isAuthenticated) {
        onRegisterSuccess()
        return
    }

    SelvaScaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            SelvaLogo(size = LogoDisplaySize.Auth)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Crear cuenta",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))

            SelvaTextField(
                value = uiState.nombre,
                onValueChange = viewModel::updateNombre,
                label = "Nombre completo",
                error = uiState.nombreError
            )
            Spacer(modifier = Modifier.height(12.dp))
            SelvaTextField(
                value = uiState.email,
                onValueChange = viewModel::updateEmail,
                label = "Correo electrónico",
                keyboardType = KeyboardType.Email,
                error = uiState.emailError
            )
            Spacer(modifier = Modifier.height(12.dp))
            SelvaTextField(
                value = uiState.password,
                onValueChange = viewModel::updatePassword,
                label = "Contraseña",
                isPassword = true,
                error = uiState.passwordError
            )
            Spacer(modifier = Modifier.height(12.dp))
            SelvaTextField(
                value = uiState.confirmPassword,
                onValueChange = viewModel::updateConfirmPassword,
                label = "Confirmar contraseña",
                isPassword = true,
                error = uiState.confirmPasswordError
            )
            if (uiState.error != null) {
                ErrorMessage(uiState.error!!)
            }
            if (uiState.termsError != null) {
                ErrorMessage(uiState.termsError!!)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Start
            ) {
                Checkbox(
                    checked = uiState.termsAccepted,
                    onCheckedChange = viewModel::updateTermsAccepted,
                    enabled = uiState.termsViewed && !uiState.isLoading
                )
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = "He leído y acepto los",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Términos y Condiciones",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { viewModel.openTermsDialog() }
                    )
                    if (!uiState.termsViewed) {
                        Text(
                            text = "Toque el enlace para leerlos antes de aceptar.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (uiState.showTermsDialog) {
                TermsAndConditionsDialog(
                    onDismiss = viewModel::dismissTermsDialog,
                    onAccept = viewModel::acceptTermsFromDialog
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            SelvaButton(
                text = if (uiState.isLoading) "Registrando..." else "Registrarse",
                onClick = viewModel::register,
                enabled = !uiState.isLoading && uiState.termsAccepted
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onNavigateToLogin) {
                Text("¿Ya tienes cuenta? Inicia sesión")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
