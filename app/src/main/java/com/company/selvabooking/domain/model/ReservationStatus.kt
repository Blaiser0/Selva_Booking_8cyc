package com.company.selvabooking.domain.model

enum class ReservationStatus(val value: String) {
    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    CANCELADA("Cancelada"),
    COMPLETADA("Completada");

    companion object {
        fun fromString(value: String): ReservationStatus {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: PENDIENTE
        }
    }
}
