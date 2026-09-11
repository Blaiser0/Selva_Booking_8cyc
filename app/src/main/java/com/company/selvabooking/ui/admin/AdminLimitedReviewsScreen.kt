package com.company.selvabooking.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.company.selvabooking.ui.components.ResenaCard
import com.company.selvabooking.ui.components.SelvaScaffold
import com.company.selvabooking.ui.components.SelvaTopAppBar
import com.company.selvabooking.ui.theme.ForestGreen
import com.company.selvabooking.viewmodel.AdminLimitedReviewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLimitedReviewsScreen(viewModel: AdminLimitedReviewsViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    SelvaScaffold(
        topBar = { SelvaTopAppBar(title = "Comentarios de hoteles") }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingIndicator(Modifier.padding(padding))
            uiState.accessDenied -> {
                ReviewsEmptyState(
                    padding = padding,
                    title = "No tiene permiso para ver comentarios"
                )
            }
            uiState.hotelCount == 0 -> {
                ReviewsEmptyState(
                    padding = padding,
                    title = "Sin hoteles en su red",
                    subtitle = "Cuando sus encargados registren hoteles, podrá consultar sus comentarios aquí."
                )
            }
            uiState.resenas.isEmpty() -> {
                ReviewsEmptyState(
                    padding = padding,
                    title = "Aún no hay comentarios",
                    subtitle = "Los huéspedes pueden dejar opiniones después de completar una estadía."
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Text(
                        text = buildString {
                            append("${uiState.resenas.size} comentario")
                            if (uiState.resenas.size != 1) append("s")
                            append(" en ${uiState.hotelCount} hotel")
                            if (uiState.hotelCount != 1) append("es")
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = ForestGreen
                    )
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.resenas, key = { it.resena.id }) { item ->
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = item.hotelName.ifBlank { "Hotel" },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ForestGreen,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                                ResenaCard(resena = item.resena)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewsEmptyState(
    padding: PaddingValues,
    title: String,
    subtitle: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
