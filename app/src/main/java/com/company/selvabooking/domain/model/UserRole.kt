package com.company.selvabooking.domain.model

enum class UserRole(val value: String, val displayLabel: String = value) {
    CLIENTE("Cliente"),
    SUPER_ADMIN("SuperAdmin", "SuperAdmin"),
    ADMINISTRADOR("Administrador", "Administrador"),
    GERENTE_HOTEL("EncargadoHotel", "Encargado del Hotel");

    fun hasAdminPanelAccess(): Boolean =
        this == SUPER_ADMIN || this == ADMINISTRADOR

    fun hasFullAdminPowers(): Boolean = this == SUPER_ADMIN

    fun isLimitedAdmin(): Boolean = this == ADMINISTRADOR

    companion object {
        private const val LEGACY_GERENTE_VALUE = "GerenteHotel"

        fun fromString(value: String): UserRole {
            if (matchesEncargadoHotel(value)) return GERENTE_HOTEL
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: CLIENTE
        }

        fun matchesEncargadoHotel(value: String): Boolean =
            value.equals(GERENTE_HOTEL.value, ignoreCase = true) ||
                value.equals(LEGACY_GERENTE_VALUE, ignoreCase = true)
    }
}
