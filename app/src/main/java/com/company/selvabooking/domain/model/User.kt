package com.company.selvabooking.domain.model

import com.company.selvabooking.utils.Constants

data class User(
    val id: String = "",
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val fotoUrl: String = "",
    val rol: UserRole = UserRole.CLIENTE,
    val solicitudAdmin: String = "",
    val puedeAlternarRol: Boolean = false
) {
    val hasPendingAdminRequest: Boolean
        get() = solicitudAdmin == Constants.ADMIN_REQUEST_PENDING

    val hasRejectedAdminRequest: Boolean
        get() = solicitudAdmin == Constants.ADMIN_REQUEST_REJECTED

    fun toMap(): Map<String, Any> = buildMap {
        put("nombre", nombre)
        put("email", email)
        put("telefono", telefono)
        put("rol", rol.value)
        put("puedeAlternarRol", puedeAlternarRol)
        if (fotoUrl.isNotBlank()) put("fotoUrl", fotoUrl)
        if (solicitudAdmin.isNotBlank()) put("solicitudAdmin", solicitudAdmin)
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): User = User(
            id = id,
            nombre = map["nombre"] as? String ?: "",
            email = map["email"] as? String ?: "",
            telefono = map["telefono"] as? String ?: "",
            fotoUrl = map["fotoUrl"] as? String ?: "",
            rol = UserRole.fromString(map["rol"] as? String ?: ""),
            solicitudAdmin = map["solicitudAdmin"] as? String ?: "",
            puedeAlternarRol = map["puedeAlternarRol"] as? Boolean
                ?: (UserRole.fromString(map["rol"] as? String ?: "") == UserRole.ADMINISTRADOR)
        )
    }
}
