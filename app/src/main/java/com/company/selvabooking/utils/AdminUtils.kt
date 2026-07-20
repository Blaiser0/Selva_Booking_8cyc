package com.company.selvabooking.utils

object AdminUtils {
    private val designatedAdminNames = listOf(
        "Jesús Uceda",
        "Jesus Uceda"
    )

    fun isDesignatedAdmin(nombre: String): Boolean {
        val normalized = nombre.trim()
        return designatedAdminNames.any { it.equals(normalized, ignoreCase = true) }
    }
}
