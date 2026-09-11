package com.company.selvabooking.navigation

import com.company.selvabooking.domain.model.User
import com.company.selvabooking.domain.model.UserRole

fun User.mainDestination(): String = when (rol) {
    UserRole.SUPER_ADMIN, UserRole.ADMINISTRADOR -> Routes.ADMIN_DASHBOARD
    UserRole.GERENTE_HOTEL -> {
        if (needsProfileCompletion) Routes.GERENTE_COMPLETE_PROFILE
        else Routes.MANAGER_HOTELS
    }
    else -> Routes.CLIENT_HOME
}

fun User.homeRoute(): String = when (rol) {
    UserRole.SUPER_ADMIN, UserRole.ADMINISTRADOR -> Routes.ADMIN_DASHBOARD
    UserRole.GERENTE_HOTEL -> Routes.MANAGER_HOTELS
    else -> Routes.CLIENT_HOME
}

fun UserRole.isStaffRole(): Boolean =
    hasAdminPanelAccess() || this == UserRole.GERENTE_HOTEL
