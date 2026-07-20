package com.company.selvabooking.domain.model

enum class UserRole(val value: String) {
    CLIENTE("Cliente"),
    ADMINISTRADOR("Administrador");

    companion object {
        fun fromString(value: String): UserRole {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: CLIENTE
        }
    }
}
