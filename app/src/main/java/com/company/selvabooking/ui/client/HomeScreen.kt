package com.company.selvabooking.ui.client

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.company.selvabooking.ui.components.HotelCard
import com.company.selvabooking.ui.components.LoadingIndicator
import com.company.selvabooking.ui.components.SelvaScaffold
import com.company.selvabooking.ui.components.SelvaTopAppBar
import com.company.selvabooking.ui.components.HotelOfferCard
import com.company.selvabooking.ui.theme.ForestGreen
import com.company.selvabooking.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onHotelClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onReservationsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    SelvaScaffold(
        topBar = {
            SelvaTopAppBar(title = "Selva Booking")
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingIndicator(Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::updateSearchQuery,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSearchClick() },
                        enabled = false,
                        placeholder = { Text("Buscar hoteles por ciudad o nombre...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = ForestGreen)
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            disabledContainerColor = MaterialTheme.colorScheme.background
                        )
                    )
                }

                item {
                    QuickAccessCard(
                        title = "Mis Reservas",
                        subtitle = "Consulta y gestiona tus reservas",
                        onClick = onReservationsClick
                    )
                }

                if (uiState.featuredHotels.isNotEmpty()) {
                    item {
                        SectionTitle("Hoteles destacados")
                    }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(uiState.featuredHotels) { hotel ->
                                HotelCard(
                                    hotel = hotel,
                                    onClick = { onHotelClick(hotel.id) },
                                    modifier = Modifier.width(280.dp)
                                )
                            }
                        }
                    }
                }

                if (uiState.offerHotels.isNotEmpty()) {
                    item {
                        SectionTitle("Ofertas especiales")
                    }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(uiState.offerHotels) { hotel ->
                                HotelCard(
                                    hotel = hotel,
                                    onClick = { onHotelClick(hotel.id) },
                                    modifier = Modifier.width(280.dp)
                                )
                            }
                        }
                    }
                }

                if (uiState.recommendedHotels.isNotEmpty()) {
                    item {
                        SectionTitle("Recomendaciones")
                    }
                    items(uiState.recommendedHotels) { hotel ->
                        HotelOfferCard(
                            hotel = hotel,
                            onClick = { onHotelClick(hotel.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun QuickAccessCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Bookmark,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
