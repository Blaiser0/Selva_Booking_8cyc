package com.company.selvabooking.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

object TermsAndConditionsContent {
    const val TITLE = "Términos y Condiciones — Selva Booking"

    val sections: List<Pair<String, String>> = listOf(
        "1. Aceptación" to
            "Al crear una cuenta en Selva Booking, usted declara haber leído, comprendido y aceptado " +
            "estos Términos y Condiciones. Si no está de acuerdo, no debe registrarse ni utilizar la aplicación.",
        "2. Servicio" to
            "Selva Booking es una plataforma móvil que permite buscar, comparar y reservar alojamientos " +
            "turísticos en la región de Madre de Dios. La app actúa como intermediario entre el usuario " +
            "y los establecimientos registrados; la estadía y servicios del hotel son responsabilidad del proveedor.",
        "3. Cuenta de usuario" to
            "Debe proporcionar información veraz (nombre, correo electrónico y contraseña). Sus datos de " +
            "cuenta se almacenan de forma segura en Firebase Authentication y Cloud Firestore. Usted es " +
            "responsable de mantener la confidencialidad de su contraseña y de toda actividad en su cuenta.",
        "4. Reservas y pagos" to
            "Para confirmar una reserva de habitación, deberá completar el pago a través de una pasarela " +
            "de pagos electrónicos integrada (por ejemplo, procesadores certificados PCI-DSS). Selva Booking " +
            "no almacena números completos de tarjeta de crédito o débito en sus servidores; los datos " +
            "sensibles de pago son capturados y procesados directamente por el proveedor de la pasarela, " +
            "bajo sus propias políticas de seguridad. Usted autoriza el cargo del monto total indicado al " +
            "confirmar la reserva, incluyendo impuestos y cargos aplicables mostrados antes del pago.",
        "5. Política de cancelación" to
            "Las condiciones de cancelación o modificación de una reserva dependen del establecimiento " +
            "y se informarán antes de confirmar el pago. Selva Booking registrará el estado de su reserva " +
            "(pendiente, confirmada, cancelada) en la aplicación.",
        "6. Privacidad" to
            "Tratamos sus datos personales conforme a la legislación vigente. Utilizamos su información " +
            "para gestionar su cuenta, reservas, historial de viajes y comunicaciones relacionadas con el " +
            "servicio. No vendemos sus datos a terceros. Las imágenes de perfil se almacenan en Firebase Storage.",
        "7. Uso permitido" to
            "Queda prohibido usar la app con fines fraudulentos, suplantar identidad, interferir con el " +
            "funcionamiento del sistema o publicar contenido ilícito. Nos reservamos el derecho de suspender " +
            "cuentas que incumplan estas normas.",
        "8. Modificaciones" to
            "Podemos actualizar estos Términos y Condiciones. Los cambios relevantes se comunicarán en la " +
            "aplicación. El uso continuado del servicio tras una actualización implica la aceptación de los " +
            "nuevos términos.",
        "9. Contacto" to
            "Para consultas sobre estos términos o el tratamiento de datos de pago, puede escribir a " +
            "soporte@selvabooking.com desde la sección Soporte y Ayuda de la aplicación.",
    )
}

@Composable
fun TermsAndConditionsDialog(
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    val scrollState = rememberScrollState()
    val hasScrolledToEnd by remember {
        derivedStateOf {
            scrollState.maxValue == 0 ||
                scrollState.value >= scrollState.maxValue - 24
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = TermsAndConditionsContent.TITLE,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(scrollState)
            ) {
                TermsAndConditionsContent.sections.forEach { (heading, body) ->
                    Text(
                        text = heading,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                if (!hasScrolledToEnd) {
                    Text(
                        text = "Desplácese hasta el final para habilitar la aceptación.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onAccept, enabled = hasScrolledToEnd) {
                Text("He leído y acepto")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}
