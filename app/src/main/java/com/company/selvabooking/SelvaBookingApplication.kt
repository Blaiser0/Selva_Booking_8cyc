package com.company.selvabooking

import android.app.Application
import com.company.selvabooking.repository.AuthRepository
import com.company.selvabooking.repository.HotelRepository
import com.company.selvabooking.repository.ResenaRepository
import com.company.selvabooking.repository.ReservationRepository
import com.company.selvabooking.repository.RoomRepository
import com.company.selvabooking.repository.SavedCardRepository

class SelvaBookingApplication : Application() {
    val authRepository by lazy { AuthRepository() }
    val hotelRepository by lazy { HotelRepository() }
    val roomRepository by lazy { RoomRepository() }
    val reservationRepository by lazy { ReservationRepository() }
    val resenaRepository by lazy { ResenaRepository() }
    val savedCardRepository by lazy { SavedCardRepository(this) }
}
