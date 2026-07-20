package com.company.selvabooking.ui.client

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.company.selvabooking.ui.components.AmenityChip
import com.company.selvabooking.ui.components.BookingSectionTitle
import com.company.selvabooking.ui.components.HotelReviewDialog
import com.company.selvabooking.ui.components.LoadingIndicator
import com.company.selvabooking.ui.components.RatingBadge
import com.company.selvabooking.ui.components.ResenaCard
import com.company.selvabooking.ui.components.RoomOfferCard
import com.company.selvabooking.ui.components.SelvaButton
import com.company.selvabooking.ui.components.SelvaOutlinedButton
import com.company.selvabooking.ui.components.StickyPriceBar
import com.company.selvabooking.ui.components.SuccessMessage
import com.company.selvabooking.ui.theme.ForestGreen
import com.company.selvabooking.ui.theme.LogoBackground
import com.company.selvabooking.ui.theme.ToucanOrange
import com.company.selvabooking.utils.DateUtils
import com.company.selvabooking.viewmodel.HotelDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelDetailScreen(
    viewModel: HotelDetailViewModel,
    onBack: () -> Unit,
    onBook: (String, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val hotel = uiState.hotel
    val listState = rememberLazyListState()
    val minRoomPrice = uiState.rooms.minOfOrNull { it.precio } ?: hotel?.precioMinimo ?: 0.0
    val firstRoomId = uiState.rooms.firstOrNull()?.id

    LaunchedEffect(uiState.reviewMessage) {
        if (uiState.reviewMessage != null) {
            kotlinx.coroutines.delay(2500)
            viewModel.clearReviewMessage()
        }
    }

    if (uiState.showReviewDialog) {
        HotelReviewDialog(
            isEditing = uiState.userResena != null,
            calificacion = uiState.reviewCalificacion,
            comentario = uiState.reviewComentario,
            calificacionError = uiState.reviewCalificacionError,
            comentarioError = uiState.reviewComentarioError,
            isSubmitting = uiState.isSubmittingReview,
            onCalificacionChange = viewModel::updateReviewCalificacion,
            onComentarioChange = viewModel::updateReviewComentario,
            onDismiss = viewModel::dismissReviewDialog,
            onSubmit = viewModel::submitReview
        )
    }

    if (uiState.showDeleteReviewDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteReviewDialog,
            title = { Text("Eliminar reseña") },
            text = { Text("¿Deseas eliminar tu comentario y calificación de este hotel?") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDeleteReview) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteReviewDialog) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        containerColor = LogoBackground,
        bottomBar = {
            if (hotel != null && !uiState.isLoading) {
                StickyPriceBar(
                    label = "Desde",
                    price = DateUtils.formatCurrency(minRoomPrice),
                    buttonText = if (uiState.rooms.isEmpty()) "Sin habitaciones" else "Ver ofertas",
                    onButtonClick = {
                        firstRoomId?.let { onBook(hotel.id, it) }
                    },
                    enabled = firstRoomId != null
                )
            }
        }
    ) { padding ->
        if (uiState.isLoading || hotel == null) {
            LoadingIndicator(Modifier.padding(padding))
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        if (hotel.imagenes.isNotEmpty()) {
                            AsyncImage(
                                model = hotel.imagenes[uiState.selectedImageIndex],
                                contentDescription = hotel.nombre,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(ForestGreen.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = hotel.nombre,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.35f),
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.55f)
                                        )
                                    )
                                )
                        )

                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .padding(12.dp)
                                .align(Alignment.TopStart)
                                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(50))
                                .size(40.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = ForestGreen
                            )
                        }

                        RatingBadge(
                            score = hotel.calificacion,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                        )

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = hotel.nombre,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${hotel.ciudad} · ${hotel.direccion}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }

                    if (hotel.imagenes.size > 1) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(hotel.imagenes) { index, url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { viewModel.selectImage(index) },
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            repeat(hotel.estrellas.coerceAtMost(5)) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = ToucanOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            if (hotel.categoria.isNotBlank()) {
                                Text(
                                    text = hotel.categoria,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (hotel.oferta) {
                                Text(
                                    text = "Oferta especial",
                                    modifier = Modifier
                                        .background(ToucanOrange.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ToucanOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = hotel.descripcion,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                if (hotel.servicios.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            BookingSectionTitle(title = "Servicios del hotel")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(hotel.servicios) { servicio ->
                                    AmenityChip(text = servicio)
                                }
                            }
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BookingSectionTitle(title = "Ubicación")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = ForestGreen
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = hotel.ubicacion.ifEmpty { "${hotel.direccion}, ${hotel.ciudad}" },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                item {
                    HotelReviewsSection(
                        calificacion = hotel.calificacion,
                        resenas = uiState.resenas,
                        userResena = uiState.userResena,
                        canReview = uiState.canReview,
                        reviewMessage = uiState.reviewMessage,
                        onWriteReview = viewModel::openReviewDialog,
                        onEditReview = viewModel::openReviewDialog,
                        onDeleteReview = viewModel::requestDeleteReview
                    )
                }

                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        BookingSectionTitle(title = "Habitaciones disponibles")
                        Text(
                            text = "Compara opciones y elige la mejor oferta para tu estadía",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (uiState.rooms.isEmpty()) {
                    item {
                        Text(
                            text = "No hay habitaciones disponibles en este momento.",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(uiState.rooms) { room ->
                        RoomOfferCard(
                            room = room,
                            onBook = { onBook(hotel.id, room.id) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HotelReviewsSection(
    calificacion: Double,
    resenas: List<com.company.selvabooking.domain.model.Resena>,
    userResena: com.company.selvabooking.domain.model.Resena?,
    canReview: Boolean,
    reviewMessage: String?,
    onWriteReview: () -> Unit,
    onEditReview: () -> Unit,
    onDeleteReview: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BookingSectionTitle(title = "Comentarios y calificación")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = String.format("%.1f", calificacion),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (resenas.isEmpty()) {
                        "Sin reseñas aún"
                    } else {
                        "${resenas.size} reseña${if (resenas.size == 1) "" else "s"}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            RatingBadge(score = calificacion)
        }

        if (reviewMessage != null) {
            SuccessMessage(reviewMessage)
        }

        when {
            userResena != null -> {
                ResenaCard(resena = userResena, isOwnReview = true)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SelvaOutlinedButton(
                        text = "Modificar reseña",
                        onClick = onEditReview,
                        modifier = Modifier.weight(1f)
                    )
                    SelvaOutlinedButton(
                        text = "Eliminar",
                        onClick = onDeleteReview,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            canReview -> {
                SelvaButton(
                    text = "Escribir reseña",
                    onClick = onWriteReview
                )
            }

            else -> {
                Text(
                    text = "Solo pueden comentar quienes han reservado al menos una vez en este hotel.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        val otherResenas = if (userResena != null) {
            resenas.filter { it.id != userResena.id }
        } else {
            resenas
        }

        otherResenas.forEach { resena ->
            ResenaCard(resena = resena)
        }
    }
}
