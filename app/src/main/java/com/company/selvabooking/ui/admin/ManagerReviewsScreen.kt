package com.company.selvabooking.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.company.selvabooking.viewmodel.ManagerReviewsViewModel

@Composable
fun ManagerReviewsScreen(viewModel: ManagerReviewsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    HotelReviewsScreen(
        uiState = uiState,
        noHotelMessage = "Sin hotel registrado",
        emptyReviewsMessage =
            "Cuando los huéspedes califiquen su hotel después de una estadía, sus comentarios aparecerán aquí."
    )
}
