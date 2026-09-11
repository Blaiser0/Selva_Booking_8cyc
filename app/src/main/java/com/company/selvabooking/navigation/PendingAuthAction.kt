package com.company.selvabooking.navigation

sealed class PendingAuthAction {
    data class HotelDetail(val hotelId: String) : PendingAuthAction()
    data class Book(val hotelId: String, val roomId: String) : PendingAuthAction()
    data class Payment(val reservationId: String) : PendingAuthAction()
    data object Reservations : PendingAuthAction()
    data object Profile : PendingAuthAction()
}
