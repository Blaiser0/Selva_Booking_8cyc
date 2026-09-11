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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.company.selvabooking.domain.model.User
import com.company.selvabooking.ui.components.ErrorMessage
import com.company.selvabooking.ui.components.LoadingIndicator
import com.company.selvabooking.ui.components.LogoDisplaySize
import com.company.selvabooking.ui.components.SelvaButton
import com.company.selvabooking.ui.components.SelvaLogo
import com.company.selvabooking.ui.components.SelvaOutlinedButton
import com.company.selvabooking.ui.components.SelvaScaffold
import com.company.selvabooking.ui.components.SelvaTextField
import com.company.selvabooking.ui.theme.CreamSurfaceVariant
import com.company.selvabooking.ui.theme.ForestGreen
import com.company.selvabooking.viewmodel.AuthViewModel

@Composable
fun GerenteProfileCompletionScreen(
    viewModel: AuthViewModel,
    user: User,
    onProfileCompleted: () -> Unit,
    onSkipForNow: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(user.id) {
        viewModel.initGerenteProfileForm(user)
    }

    LaunchedEffect(uiState.currentUser?.perfilCompleto) {
        if (uiState.currentUser?.perfilCompleto == true) {
            onProfileCompleted()
        }
    }

    SelvaScaffold { padding ->
        if (uiState.isCompletingProfile) {
            LoadingIndicator(Modifier.padding(padding))
            return@SelvaScaffold
        }

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
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Bienvenido, Encargado del Hotel",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = ForestGreen
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Es su primera vez en Selva Booking. ¿Desea actualizar sus datos?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (!uiState.showGerenteProfileForm) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CreamSurfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Correo asignado: ${user.email}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Puede completar su nombre y contraseña personal ahora, o hacerlo más tarde.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        SelvaButton(
                            text = "Sí, actualizar mis datos",
                            onClick = viewModel::showGerenteProfileForm
                        )
                        SelvaOutlinedButton(
                            text = "Omitir por ahora",
                            onClick = onSkipForNow
                        )
                    }
                }
            } else {
                SelvaTextField(
                    value = uiState.nombre,
                    onValueChange = viewModel::updateNombre,
                    label = "Nombre completo",
                    error = uiState.nombreError
                )
                Spacer(modifier = Modifier.height(12.dp))
                SelvaTextField(
                    value = uiState.email,
                    onValueChange = {},
                    label = "Correo electrónico",
                    enabled = false
                )
                Spacer(modifier = Modifier.height(12.dp))
                SelvaTextField(
                    value = uiState.password,
                    onValueChange = viewModel::updatePassword,
                    label = "Nueva contraseña",
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

                if (uiState.error != null) ErrorMessage(uiState.error!!)
                if (uiState.termsError != null) ErrorMessage(uiState.termsError!!)

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = uiState.termsAccepted,
                        onCheckedChange = viewModel::updateTermsAccepted,
                        enabled = uiState.termsViewed && !uiState.isCompletingProfile
                    )
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Text(
                            text = "He leído y acepto los",
                            style = MaterialTheme.typography.bodyMedium
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
                                text = "Debe leer los términos antes de aceptar.",
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
                    text = "Guardar cambios",
                    onClick = viewModel::completeGerenteProfile,
                    enabled = uiState.termsAccepted && !uiState.isCompletingProfile
                )
                TextButton(onClick = viewModel::hideGerenteProfileForm) {
                    Text("Volver")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
