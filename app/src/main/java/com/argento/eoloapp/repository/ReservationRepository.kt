package com.argento.eoloapp.repository

import com.argento.eoloapp.api.ApiService
import com.argento.eoloapp.data.CobrarReservationRequest

class ReservationRepository(private val apiService: ApiService) {

    suspend fun cobrarReservation(token: String, request: CobrarReservationRequest) {
        apiService.cobrarReservation(token, request)
    }
}