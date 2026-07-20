package com.company.selvabooking.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.company.selvabooking.ui.components.LoadingIndicator
import com.company.selvabooking.ui.components.SelvaScaffold
import com.company.selvabooking.ui.components.SelvaTopAppBar
import com.company.selvabooking.ui.components.StatCard
import com.company.selvabooking.ui.theme.CreamSurfaceVariant
import com.company.selvabooking.ui.theme.CreamSurfaceVariant
import com.company.selvabooking.ui.theme.ForestGreen
import com.company.selvabooking.viewmodel.AdminDashboardViewModel

data class StatItem(val title: String, val value: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel,
    onNavigateToRequests: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val stats = listOf(
        StatItem("Hoteles", uiState.totalHotels.toString()),
        StatItem("Habitaciones", uiState.totalRooms.toString()),
        StatItem("Reservas", uiState.totalReservations.toString()),
        StatItem("Reservas activas", uiState.activeReservations.toString()),
        StatItem("Usuarios", uiState.totalUsers.toString()),
        StatItem("Solicitudes admin", uiState.pendingAdminRequests.toString())
    )

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
                    .padding(16.dp)
            ) {
                if (uiState.pendingAdminRequests > 0) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateToRequests),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CreamSurfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Solicitudes de administrador",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                                Text(
                                    text = "${uiState.pendingAdminRequests} usuario(s) esperan tu revisión",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = ForestGreen
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = "Estadísticas generales",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(stats) { stat ->
                        StatCard(
                            title = stat.title,
                            value = stat.value,
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (stat.title == "Solicitudes admin" && uiState.pendingAdminRequests > 0) {
                                        Modifier.clickable(onClick = onNavigateToRequests)
                                    } else {
                                        Modifier
                                    }
                                )
                        )
                    }
                }
            }
        }
    }
}
