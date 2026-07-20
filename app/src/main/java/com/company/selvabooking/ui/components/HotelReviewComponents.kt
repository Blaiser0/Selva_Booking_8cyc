package com.company.selvabooking.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.company.selvabooking.domain.model.Resena
import com.company.selvabooking.ui.theme.ToucanOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StarRatingSelector(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    val selectedRating = rating.coerceIn(1, 5)

    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            (1..5).forEach { star ->
                IconButton(
                    onClick = { onRatingChange(star) },
                    modifier = Modifier.size(40.dp)
                ) {
                    StarIcon(
                        filled = star <= selectedRating,
                        contentDescription = "$star estrellas"
                    )
                }
            }
            Text(
                text = "$selectedRating/5",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun ResenaCard(
    resena: Resena,
    modifier: Modifier = Modifier,
    isOwnReview: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOwnReview) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = resena.userNombre.ifBlank { "Huésped" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatResenaDate(resena.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StarRatingDisplay(rating = resena.calificacion)
            Text(
                text = resena.comentario,
                style = MaterialTheme.typography.bodyMedium
            )
            if (isOwnReview) {
                Text(
                    text = "Tu reseña",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun StarRatingDisplay(
    rating: Int,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    val safeRating = rating.coerceIn(1, 5)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        (1..5).forEach { star ->
            StarIcon(
                filled = star <= safeRating,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
        if (showLabel) {
            Text(
                text = "$safeRating/5",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start = 6.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StarIcon(
    filled: Boolean,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    if (filled) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = contentDescription,
            tint = ToucanOrange,
            modifier = modifier
        )
    } else {
        Icon(
            imageVector = Icons.Outlined.StarOutline,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.outline,
            modifier = modifier
        )
    }
}

@Composable
fun HotelReviewDialog(
    isEditing: Boolean,
    calificacion: Int,
    comentario: String,
    calificacionError: String?,
    comentarioError: String?,
    isSubmitting: Boolean,
    onCalificacionChange: (Int) -> Unit,
    onComentarioChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isEditing) "Modificar reseña" else "Escribir reseña")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Califica tu experiencia en este hotel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StarRatingSelector(
                    rating = calificacion,
                    onRatingChange = onCalificacionChange,
                    error = calificacionError
                )
                SelvaTextField(
                    value = comentario,
                    onValueChange = onComentarioChange,
                    label = "Comentario",
                    error = comentarioError,
                    singleLine = false,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSubmit,
                enabled = !isSubmitting
            ) {
                Text(
                    text = if (isSubmitting) "Guardando..." else "Publicar",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Cancelar")
            }
        }
    )
}

private fun formatResenaDate(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    return SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("es-PE")).format(Date(timestamp))
}
