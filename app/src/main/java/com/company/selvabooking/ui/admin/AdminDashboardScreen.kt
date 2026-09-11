package com.company.selvabooking.ui.admin

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.company.selvabooking.ui.components.LoadingIndicator
import com.company.selvabooking.ui.components.SelvaScaffold
import com.company.selvabooking.ui.components.SelvaTopAppBar
import com.company.selvabooking.ui.theme.CreamSurfaceVariant
import com.company.selvabooking.ui.theme.ForestGreen
import com.company.selvabooking.ui.theme.ForestGreenDark
import com.company.selvabooking.ui.theme.LightText
import com.company.selvabooking.ui.theme.TropicalGreen
import com.company.selvabooking.utils.DateUtils
import com.company.selvabooking.viewmodel.AdminDashboardUiState
import com.company.selvabooking.viewmodel.AdminDashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel,
    onNavigateToReservations: () -> Unit = {},
    onNavigateToHotels: () -> Unit = {},
    onNavigateToGerentes: () -> Unit = {},
    onNavigateToAdministradores: () -> Unit = {},
    onNavigateToUsers: () -> Unit = {},
    onNavigateToAudit: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    SelvaScaffold(
        topBar = { SelvaTopAppBar(title = "Dashboard") }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingIndicator(Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardHeader(uiState = uiState)

                if (uiState.isLimitedAdmin) {
                    DashboardScopeBanner(
                        gerentesCount = uiState.totalGerentes,
                        hotelsCount = uiState.totalHotels
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardHeroCard(
                        title = "Ingresos",
                        value = DateUtils.formatCurrency(uiState.totalRevenue),
                        subtitle = "Reservas confirmadas y terminadas",
                        icon = Icons.Default.AttachMoney,
                        modifier = Modifier.weight(1f),
                        background = Brush.linearGradient(
                            listOf(ForestGreen, ForestGreenDark)
                        )
                    )
                    DashboardHeroCard(
                        title = "Reservas",
                        value = uiState.totalReservations.toString(),
                        subtitle = "${uiState.confirmedReservations} confirmadas · ${uiState.terminatedReservations} terminadas",
                        icon = Icons.AutoMirrored.Filled.List,
                        modifier = Modifier.weight(1f),
                        background = Brush.linearGradient(
                            listOf(TropicalGreen, ForestGreen)
                        ),
                        onClick = onNavigateToReservations
                    )
                }

                DashboardSectionTitle("Plataforma")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardMetricCard(
                        title = if (uiState.isSuperAdmin) "Gestionar hoteles" else "Hoteles registrados",
                        value = uiState.totalHotels.toString(),
                        icon = Icons.Default.Hotel,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToHotels
                    )
                    DashboardMetricCard(
                        title = "Habitaciones",
                        value = uiState.totalRooms.toString(),
                        subtitle = "Administrar desde cada hotel",
                        icon = Icons.Default.MeetingRoom,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardMetricCard(
                        title = "Encargados",
                        value = uiState.totalGerentes.toString(),
                        icon = Icons.Default.Person,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToGerentes
                    )
                    if (uiState.isSuperAdmin) {
                        DashboardMetricCard(
                            title = "Administradores",
                            value = uiState.totalAdministradores.toString(),
                            icon = Icons.Default.AdminPanelSettings,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToAdministradores
                        )
                    } else {
                        DashboardMetricCard(
                            title = "Reservas confirmadas",
                            value = uiState.confirmedReservations.toString(),
                            icon = Icons.AutoMirrored.Filled.List,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToReservations
                        )
                    }
                }
                if (uiState.isSuperAdmin) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DashboardMetricCard(
                            title = "Usuarios",
                            value = uiState.totalUsuarios.toString(),
                            subtitle = "Ver todos y filtrar por rol",
                            icon = Icons.Default.People,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToUsers
                        )
                    }
                }

                DashboardSectionTitle("Estado de reservas")
                ReservationBreakdownCard(
                    uiState = uiState,
                    onClick = onNavigateToReservations
                )

                DashboardSectionTitle("Accesos rápidos")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickLinkRow(
                        label = if (uiState.isSuperAdmin) "Gestionar hoteles" else "Consultar hoteles",
                        subtitle = if (uiState.isSuperAdmin) {
                            "Crear, editar, habitaciones, filtro por administrador y comentarios (estrella)"
                        } else {
                            "Consulta de hoteles y comentarios (icono estrella)"
                        },
                        icon = Icons.Default.Hotel,
                        onClick = onNavigateToHotels
                    )
                    QuickLinkRow(
                        label = if (uiState.isSuperAdmin) "Gestionar reservas" else "Ver reservas",
                        subtitle = if (uiState.isSuperAdmin) {
                            "Crear, editar, eliminar y filtrar"
                        } else {
                            "Confirmadas y terminadas con filtros"
                        },
                        icon = Icons.AutoMirrored.Filled.List,
                        onClick = onNavigateToReservations
                    )
                    QuickLinkRow(
                        label = "Encargados del hotel",
                        subtitle = if (uiState.isSuperAdmin) {
                            "Crear y eliminar cuentas de encargado en todo el sistema"
                        } else {
                            "Crear y eliminar encargados bajo su administración"
                        },
                        icon = Icons.Default.Person,
                        onClick = onNavigateToGerentes
                    )
                    if (uiState.isSuperAdmin) {
                        QuickLinkRow(
                            label = "Administradores",
                            subtitle = "Crear cuentas de administrador",
                            icon = Icons.Default.AdminPanelSettings,
                            onClick = onNavigateToAdministradores
                        )
                        QuickLinkRow(
                            label = "Registro y respaldos",
                            subtitle = "Ver acciones de encargados y revertir cambios",
                            icon = Icons.Default.History,
                            onClick = onNavigateToAudit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DashboardHeader(uiState: AdminDashboardUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(ForestGreen, TropicalGreen)
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = when {
                    uiState.isSuperAdmin -> "Panel SuperAdmin"
                    uiState.isLimitedAdmin -> "Panel de administrador"
                    else -> "Panel de administración"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = LightText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when {
                    uiState.isSuperAdmin ->
                        "Control total: hoteles, reservas, administradores, encargados y comentarios"
                    uiState.isLimitedAdmin ->
                        "Supervisión de sus encargados, hoteles y reservas (solo consulta en hoteles)"
                    else ->
                        "Supervisión y gestión del sistema"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = LightText.copy(alpha = 0.88f)
            )
        }
    }
}

@Composable
private fun DashboardScopeBanner(gerentesCount: Int, hotelsCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CreamSurfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Alcance de su cuenta",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = ForestGreen
            )
            Text(
                text = "Mostrando datos de $gerentesCount encargado${if (gerentesCount == 1) "" else "s"} " +
                    "y $hotelsCount hotel${if (hotelsCount == 1) "" else "es"} bajo su gestión.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DashboardSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = ForestGreen
    )
}

@Composable
private fun DashboardHeroCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    background: Brush,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(background)
                .padding(16.dp)
        ) {
            Column {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = LightText.copy(alpha = 0.9f),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = LightText
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = LightText.copy(alpha = 0.92f)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = LightText.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
private fun DashboardMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CreamSurfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = ForestGreen)
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = ForestGreen
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun ReservationBreakdownCard(
    uiState: AdminDashboardUiState,
    onClick: () -> Unit = {}
) {
    val total = uiState.totalReservations.coerceAtLeast(1)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CreamSurfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ReservationStatusRow(
                label = "Confirmadas",
                count = uiState.confirmedReservations,
                total = total,
                color = ForestGreen
            )
            ReservationStatusRow(
                label = "Terminadas",
                count = uiState.terminatedReservations,
                total = total,
                color = TropicalGreen
            )
        }
    }
}

@Composable
private fun ReservationStatusRow(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        LinearProgressIndicator(
            progress = { count.toFloat() / total.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}

@Composable
private fun QuickLinkRow(
    label: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = ForestGreen)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ForestGreen)
        }
    }
}
