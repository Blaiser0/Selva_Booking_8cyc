package com.company.selvabooking.domain.model

enum class ReservationStatus(
    val value: String,
    val isPublic: Boolean = true
) {
    /** Reserva creada, pendiente de pago — no se muestra como categoría al usuario. */
    AWAITING_PAYMENT("Pendiente de pago", isPublic = false),
    CONFIRMADA("Confirmada"),
    TERMINADA("Terminada");

    fun holdsStock(): Boolean = this == CONFIRMADA

    companion object {
        val publicStatuses: List<ReservationStatus> =
            entries.filter { it.isPublic }

        fun fromString(value: String): ReservationStatus {
            return when {
                value.equals("Confirmada", ignoreCase = true) -> CONFIRMADA
                value.equals("Terminada", ignoreCase = true) -> TERMINADA
                value.equals("Completada", ignoreCase = true) -> TERMINADA
                value.equals("Pendiente de pago", ignoreCase = true) -> AWAITING_PAYMENT
                value.equals("Pendiente", ignoreCase = true) -> AWAITING_PAYMENT
                value.equals("Cancelada", ignoreCase = true) -> TERMINADA
                else -> AWAITING_PAYMENT
            }
        }
    }
}
