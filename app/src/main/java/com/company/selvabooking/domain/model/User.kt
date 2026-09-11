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
    val puedeAlternarRol: Boolean = false,
    val perfilCompleto: Boolean = true,
    val rolAlternativo: String = "",
    val creadoPorAdminId: String = ""
) {
    val needsProfileCompletion: Boolean
        get() = rol == UserRole.GERENTE_HOTEL && !perfilCompleto
    val hasPendingAdminRequest: Boolean
        get() = solicitudAdmin == Constants.ADMIN_REQUEST_PENDING

    val hasRejectedAdminRequest: Boolean
        get() = solicitudAdmin == Constants.ADMIN_REQUEST_REJECTED

    val canSwitchAccountType: Boolean
        get() = when (rol) {
            UserRole.SUPER_ADMIN, UserRole.ADMINISTRADOR, UserRole.GERENTE_HOTEL -> true
            UserRole.CLIENTE -> puedeAlternarRol ||
                UserRole.matchesEncargadoHotel(rolAlternativo) ||
                rolAlternativo == UserRole.ADMINISTRADOR.value ||
                rolAlternativo == UserRole.SUPER_ADMIN.value
        }

    /** Cuenta de administrador activa o temporalmente en modo cliente. */
    val isAdministratorAccount: Boolean
        get() = rol.hasAdminPanelAccess() ||
            (
                rol == UserRole.CLIENTE &&
                    puedeAlternarRol &&
                    (
                        rolAlternativo == UserRole.ADMINISTRADOR.value ||
                            rolAlternativo == UserRole.SUPER_ADMIN.value
                        )
                )

    /** Id persistente del administrador para encargados creados por esta cuenta. */
    val administratorOwnerId: String?
        get() = when {
            rol == UserRole.ADMINISTRADOR || rol == UserRole.SUPER_ADMIN -> id
            isAdministratorAccount -> id
            else -> null
        }

    fun toMap(): Map<String, Any> = buildMap {
        put("nombre", nombre)
        put("email", email)
        put("telefono", telefono)
        put("rol", rol.value)
        put("puedeAlternarRol", puedeAlternarRol)
        put("perfilCompleto", perfilCompleto)
        if (fotoUrl.isNotBlank()) put("fotoUrl", fotoUrl)
        if (solicitudAdmin.isNotBlank()) put("solicitudAdmin", solicitudAdmin)
        if (rolAlternativo.isNotBlank()) put("rolAlternativo", rolAlternativo)
        if (creadoPorAdminId.isNotBlank()) put("creadoPorAdminId", creadoPorAdminId)
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
                ?: UserRole.fromString(map["rol"] as? String ?: "").hasAdminPanelAccess(),
            perfilCompleto = map["perfilCompleto"] as? Boolean ?: true,
            rolAlternativo = map["rolAlternativo"] as? String ?: "",
            creadoPorAdminId = map["creadoPorAdminId"] as? String ?: ""
        )
    }
}
