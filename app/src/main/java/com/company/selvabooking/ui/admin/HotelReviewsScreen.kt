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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.company.selvabooking.ui.components.LoadingIndicator
import com.company.selvabooking.ui.components.ResenaCard
import com.company.selvabooking.ui.components.SelvaScaffold
import com.company.selvabooking.ui.components.SelvaTopAppBar
import com.company.selvabooking.ui.theme.ForestGreen
import com.company.selvabooking.viewmodel.HotelReviewsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelReviewsScreen(
    uiState: HotelReviewsUiState,
    onBack: (() -> Unit)? = null,
    noHotelMessage: String = "Hotel no encontrado",
    emptyReviewsMessage: String =
        "Cuando los huéspedes califiquen este hotel después de una estadía, sus comentarios aparecerán aquí."
) {
    SelvaScaffold(
        topBar = {
            SelvaTopAppBar(
                title = if (uiState.hotelName.isBlank()) {
                    "Comentarios de huéspedes"
                } else {
                    "Comentarios · ${uiState.hotelName}"
                },
                showDrawerMenu = onBack == null,
                navigationIcon = onBack?.let { back ->
                    {
                        IconButton(onClick = back) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingIndicator(Modifier.padding(padding))
            uiState.accessDenied -> {
                HotelReviewsMessage(
                    padding = padding,
                    title = "No tiene permiso para ver comentarios"
                )
            }
            uiState.noHotel -> {
                HotelReviewsMessage(
                    padding = padding,
                    title = noHotelMessage
                )
            }
            uiState.resenas.isEmpty() -> {
                HotelReviewsMessage(
                    padding = padding,
                    title = "Aún no hay comentarios",
                    subtitle = emptyReviewsMessage
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Text(
                        text = "${uiState.resenas.size} comentario${if (uiState.resenas.size == 1) "" else "s"}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = ForestGreen
                    )
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.resenas, key = { it.id }) { resena ->
                            ResenaCard(resena = resena)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HotelReviewsMessage(
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
