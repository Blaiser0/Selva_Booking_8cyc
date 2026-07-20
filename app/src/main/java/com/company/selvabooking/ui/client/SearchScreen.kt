package com.company.selvabooking.ui.client

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.company.selvabooking.ui.components.BookingSearchBar
import com.company.selvabooking.ui.components.FilterToggle
import com.company.selvabooking.ui.components.HotelOfferCard
import com.company.selvabooking.ui.components.LoadingIndicator
import com.company.selvabooking.ui.components.SelvaScaffold
import com.company.selvabooking.ui.components.SelvaTextField
import com.company.selvabooking.ui.components.SelvaTopAppBar
import com.company.selvabooking.ui.components.SortFilterChip
import com.company.selvabooking.ui.components.StarFilterChip
import com.company.selvabooking.ui.theme.LogoBackground
import com.company.selvabooking.viewmodel.HotelSortOption
import com.company.selvabooking.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onHotelClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    SelvaScaffold(
        topBar = { SelvaTopAppBar(title = "Comparar hoteles") }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingIndicator(Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(LogoBackground)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BookingSearchBar(
                        query = uiState.query,
                        onQueryChange = viewModel::updateQuery,
                        placeholder = "Hotel, ciudad o destino..."
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${uiState.filteredHotels.size} ofertas encontradas",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FilterToggle(
                            expanded = uiState.filtersExpanded,
                            onToggle = viewModel::toggleFilters
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SortFilterChip(
                            label = "Recomendados",
                            selected = uiState.sortBy == HotelSortOption.RECOMMENDED,
                            onClick = { viewModel.updateSortBy(HotelSortOption.RECOMMENDED) }
                        )
                        SortFilterChip(
                            label = "Menor precio",
                            selected = uiState.sortBy == HotelSortOption.PRICE_ASC,
                            onClick = { viewModel.updateSortBy(HotelSortOption.PRICE_ASC) }
                        )
                        SortFilterChip(
                            label = "Mejor valoración",
                            selected = uiState.sortBy == HotelSortOption.RATING_DESC,
                            onClick = { viewModel.updateSortBy(HotelSortOption.RATING_DESC) }
                        )
                    }

                    AnimatedVisibility(
                        visible = uiState.filtersExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SelvaTextField(
                                value = uiState.ciudad,
                                onValueChange = viewModel::updateCiudad,
                                label = "Ciudad"
                            )
                            SelvaTextField(
                                value = uiState.precioMax,
                                onValueChange = viewModel::updatePrecioMax,
                                label = "Precio máximo por noche",
                                keyboardType = KeyboardType.Number
                            )
                            Text(
                                text = "Estrellas mínimas",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                (0..5).forEach { stars ->
                                    StarFilterChip(
                                        stars = stars,
                                        selected = uiState.estrellasMin == stars,
                                        onClick = { viewModel.updateEstrellasMin(stars) }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (uiState.filteredHotels.isEmpty()) {
                        item {
                            Text(
                                text = "No encontramos hoteles con esos criterios. Prueba ajustando los filtros.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 24.dp)
                            )
                        }
                    } else {
                        items(uiState.filteredHotels) { hotel ->
                            HotelOfferCard(
                                hotel = hotel,
                                onClick = { onHotelClick(hotel.id) }
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}
