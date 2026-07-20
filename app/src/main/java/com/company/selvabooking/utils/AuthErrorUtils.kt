package com.company.selvabooking.utils

import com.google.firebase.auth.FirebaseAuthException

object AuthErrorUtils {

    fun toUserMessage(error: Throwable): String {
        val code = (error as? FirebaseAuthException)?.errorCode
        return when (code) {
            "ERROR_INVALID_EMAIL" -> "Correo electrónico inválido"
            "ERROR_USER_NOT_FOUND" -> "No existe una cuenta con este correo"
            "ERROR_WRONG_PASSWORD" -> "Contraseña incorrecta"
            "ERROR_INVALID_CREDENTIAL",
            "ERROR_INVALID_LOGIN_CREDENTIALS" -> "Correo o contraseña incorrectos"
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Este correo ya está registrado"
            "ERROR_WEAK_PASSWORD" -> "La contraseña debe tener al menos 6 caracteres"
            "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos. Intenta más tarde"
            "ERROR_USER_DISABLED" -> "Esta cuenta ha sido deshabilitada"
            else -> {
                val message = error.message.orEmpty()
                when {
                    message.contains("Usuario no encontrado", ignoreCase = true) ->
                        "Tu cuenta existe pero falta el perfil. Se creará automáticamente al iniciar sesión."
                    message.contains("PERMISSION_DENIED", ignoreCase = true) ->
                        "Sin permiso para acceder a los datos. Revisa las reglas de Firestore."
                    message.isNotBlank() -> message
                    else -> "Error de autenticación"
                }
            }
        }
    }
}
