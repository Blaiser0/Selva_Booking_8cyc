package com.company.selvabooking.utils

import com.company.selvabooking.domain.model.Hotel
import com.company.selvabooking.domain.model.Reservation
import com.company.selvabooking.domain.model.Room
import com.company.selvabooking.domain.model.User
import com.company.selvabooking.domain.model.UserRole

object AdminDataScope {

    fun gerentesCreatedBy(adminId: String, gerentes: List<User>): List<User> =
        gerentes.filter { it.creadoPorAdminId == adminId }

    fun managedGerenteIds(adminId: String, gerentes: List<User>): Set<String> =
        gerentesCreatedBy(adminId, gerentes).map { it.id }.toSet()

    fun hotelsForAdmin(adminId: String, gerentes: List<User>, hotels: List<Hotel>): List<Hotel> {
        val ownerIds = managedGerenteIds(adminId, gerentes)
        return hotels.filter { it.propietarioId in ownerIds }
    }

    fun hotelIdsForAdmin(adminId: String, gerentes: List<User>, hotels: List<Hotel>): Set<String> =
        hotelsForAdmin(adminId, gerentes, hotels).map { it.id }.toSet()

    fun roomsForAdmin(
        adminId: String,
        gerentes: List<User>,
        hotels: List<Hotel>,
        rooms: List<Room>
    ): List<Room> {
        val hotelIds = hotelIdsForAdmin(adminId, gerentes, hotels)
        return rooms.filter { it.hotelId in hotelIds }
    }

    fun reservationsForAdmin(
        adminId: String,
        gerentes: List<User>,
        hotels: List<Hotel>,
        reservations: List<Reservation>
    ): List<Reservation> {
        val hotelIds = hotelIdsForAdmin(adminId, gerentes, hotels)
        return reservations.filter { it.hotelId in hotelIds }
    }

    fun hotelsWithoutAdministrator(gerentes: List<User>, hotels: List<Hotel>): List<Hotel> =
        hotels.filter { hotel ->
            when {
                hotel.propietarioId.isBlank() -> true
                else -> {
                    val gerente = gerentes.find { it.id == hotel.propietarioId }
                    gerente == null || gerente.creadoPorAdminId.isBlank()
                }
            }
        }

    fun resolveAdministratorAccounts(
        administratorAccounts: List<User>,
        gerentes: List<User>
    ): List<User> {
        val resolved = administratorAccounts.associateBy { it.id }.toMutableMap()
        val referencedAdminIds = gerentes.map { it.creadoPorAdminId }.filter { it.isNotBlank() }.toSet()
        referencedAdminIds.forEach { adminId ->
            if (adminId !in resolved) {
                resolved[adminId] = User(
                    id = adminId,
                    nombre = "Administrador",
                    rol = UserRole.ADMINISTRADOR
                )
            }
        }
        return resolved.values.sortedBy { it.nombre.lowercase() }
    }
}
