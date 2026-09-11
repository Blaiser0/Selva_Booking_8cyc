package com.company.selvabooking.utils

object AdminUtils {
    private const val SUPER_ADMIN_EMAIL = "snakercher@gmail.com"

    fun isSuperAdminEmail(email: String): Boolean =
        email.trim().equals(SUPER_ADMIN_EMAIL, ignoreCase = true)
}
