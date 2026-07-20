package com.company.selvabooking.ui.support

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.company.selvabooking.ui.components.SelvaScaffold
import com.company.selvabooking.ui.components.SelvaTopAppBar
import com.company.selvabooking.ui.theme.CreamSurfaceVariant
import com.company.selvabooking.ui.theme.DarkText
import com.company.selvabooking.ui.theme.ForestGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen() {
    SelvaScaffold(
        topBar = { SelvaTopAppBar(title = "Soporte y Ayuda") }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "¿Necesitas ayuda?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = ForestGreen
            )
            Text(
                text = "Estamos aquí para ayudarte con reservas, pagos y tu cuenta.",
                style = MaterialTheme.typography.bodyMedium,
                color = DarkText.copy(alpha = 0.8f)
            )

            SupportCard(
                title = "Preguntas frecuentes",
                body = "• ¿Cómo reservo un hotel? Busca un hotel, elige habitación y confirma fechas.\n" +
                    "• ¿Puedo cancelar? Depende de la política del hotel seleccionado.\n" +
                    "• ¿Olvidé mi contraseña? Usa \"¿Olvidaste tu contraseña?\" en el inicio de sesión."
            )

            SupportCard(
                title = "Contacto",
                body = "Correo: soporte@selvabooking.com\n" +
                    "Horario: Lunes a viernes, 9:00 – 18:00"
            )

            SupportCard(
                title = "Reportar un problema",
                body = "Si encuentras un error en la app, descríbelo y envíalo a soporte@selvabooking.com " +
                    "indicando tu correo registrado."
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SupportCard(
    title: String,
    body: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CreamSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = ForestGreen
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = DarkText.copy(alpha = 0.85f)
            )
        }
    }
}
