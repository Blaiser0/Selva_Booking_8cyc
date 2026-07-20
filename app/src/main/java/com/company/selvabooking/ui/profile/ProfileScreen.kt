package com.company.selvabooking.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.company.selvabooking.domain.model.User
import com.company.selvabooking.domain.model.UserRole
import com.company.selvabooking.ui.components.ErrorMessage
import com.company.selvabooking.ui.components.LoadingIndicator
import com.company.selvabooking.ui.components.SelvaButton
import com.company.selvabooking.ui.components.SelvaOutlinedButton
import com.company.selvabooking.ui.components.SelvaScaffold
import com.company.selvabooking.ui.components.SelvaTextField
import com.company.selvabooking.ui.components.SelvaTopAppBar
import com.company.selvabooking.ui.components.SuccessMessage
import com.company.selvabooking.ui.theme.CreamSurfaceVariant
import com.company.selvabooking.ui.theme.ForestGreen
import com.company.selvabooking.ui.theme.LightText
import com.company.selvabooking.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onUserUpdated: (User) -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.uploadProfilePhoto(it, onUserUpdated) }
    }

    LaunchedEffect(uiState.user?.id, uiState.user?.rol, uiState.user?.fotoUrl) {
        uiState.user?.let { onUserUpdated(it) }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (uiState.showSwitchToClientDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissSwitchToClientDialog,
            title = { Text("Cambiar a modo cliente") },
            text = {
                Text("¿Deseas cambiar tu cuenta a modo Cliente? Podrás volver a administrador con 3 toques en el escudo.")
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmSwitchToClientRole(onUserUpdated) }) {
                    Text("Cambiar", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissSwitchToClientDialog) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (uiState.showSwitchToAdminDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissSwitchToAdminDialog,
            title = { Text("Cambiar a modo administrador") },
            text = {
                Text("¿Deseas activar el modo administrador en tu cuenta?")
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmSwitchToAdminRole(onUserUpdated) }) {
                    Text("Activar", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissSwitchToAdminDialog) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (uiState.showAdminRequestDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissAdminRequestDialog,
            title = { Text("Solicitar acceso de administrador") },
            text = {
                Text(
                    "¿Deseas solicitar acceso como administrador? " +
                        "Tu solicitud quedará pendiente de revisión."
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmAdminAccessRequest(onUserUpdated) }) {
                    Text("Solicitar", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissAdminRequestDialog) {
                    Text("Cancelar")
                }
            }
        )
    }

    SelvaScaffold(
        topBar = { SelvaTopAppBar(title = "Mi perfil") }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingIndicator(Modifier.padding(padding))
            uiState.user == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (uiState.error != null) {
                        ErrorMessage(uiState.error!!)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    SelvaButton(
                        text = "Reintentar",
                        onClick = { viewModel.loadProfile(onUserUpdated) }
                    )
                }
            }
            else -> {
                val user = uiState.user!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CreamSurfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ProfileAvatar(
                                user = user,
                                isUploading = uiState.isUploadingPhoto,
                                onChangePhoto = { imagePicker.launch("image/*") }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = user.nombre.ifBlank { "Usuario" },
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = user.rol.value,
                                style = MaterialTheme.typography.labelLarge,
                                color = ForestGreen,
                                modifier = Modifier
                                    .background(
                                        ForestGreen.copy(alpha = 0.12f),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                            if (user.hasPendingAdminRequest) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Solicitud de administrador pendiente",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Información de la cuenta",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            ProfileInfoRow(
                                icon = Icons.Default.Person,
                                label = "Nombre",
                                value = user.nombre.ifBlank { "Sin nombre" }
                            )
                            ProfileInfoRow(
                                icon = Icons.Default.Email,
                                label = "Correo",
                                value = user.email
                            )
                            ProfileInfoRow(
                                icon = Icons.Default.Shield,
                                label = "Tipo de cuenta",
                                value = when {
                                    user.rol == UserRole.ADMINISTRADOR -> user.rol.value
                                    user.hasPendingAdminRequest -> "${user.rol.value} (solicitud pendiente)"
                                    user.hasRejectedAdminRequest -> "${user.rol.value} (solicitud rechazada)"
                                    else -> user.rol.value
                                },
                                onTripleTap = viewModel::onAccountTypeTripleTap
                            )

                            if (!uiState.isEditing) {
                                SelvaOutlinedButton(
                                    text = "Editar nombre",
                                    onClick = viewModel::startEditing
                                )
                            } else {
                                SelvaTextField(
                                    value = uiState.nombre,
                                    onValueChange = viewModel::updateNombre,
                                    label = "Nombre completo",
                                    error = uiState.nombreError
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    SelvaOutlinedButton(
                                        text = "Cancelar",
                                        onClick = viewModel::cancelEditing,
                                        modifier = Modifier.weight(1f)
                                    )
                                    SelvaButton(
                                        text = if (uiState.isSaving) "Guardando..." else "Guardar",
                                        onClick = { viewModel.saveProfile(onUserUpdated) },
                                        enabled = !uiState.isSaving,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    if (uiState.successMessage != null) {
                        SuccessMessage(uiState.successMessage!!)
                    }
                    if (uiState.error != null) {
                        ErrorMessage(uiState.error!!)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Sesión",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            SelvaOutlinedButton(
                                text = "Cerrar sesión",
                                onClick = { showLogoutDialog = true }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileAvatar(
    user: User,
    isUploading: Boolean,
    onChangePhoto: () -> Unit
) {
    Box(
        modifier = Modifier.size(96.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(ForestGreen)
                .clickable(enabled = !isUploading, onClick = onChangePhoto),
            contentAlignment = Alignment.Center
        ) {
            if (user.fotoUrl.isNotBlank()) {
                AsyncImage(
                    model = user.fotoUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = user.nombre.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = LightText
                )
            }

            if (isUploading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ForestGreen.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = LightText,
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(30.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(enabled = !isUploading, onClick = onChangePhoto),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Cambiar foto",
                tint = LightText,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onTripleTap: (() -> Unit)? = null
) {
    var tapCount by remember { mutableIntStateOf(0) }
    var lastTapTime by remember { mutableLongStateOf(0L) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onTripleTap != null) {
                    Modifier.clickable {
                        val now = System.currentTimeMillis()
                        if (now - lastTapTime > 2000L) {
                            tapCount = 0
                        }
                        lastTapTime = now
                        tapCount++
                        if (tapCount >= 3) {
                            tapCount = 0
                            onTripleTap()
                        }
                    }
                } else {
                    Modifier
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ForestGreen,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
