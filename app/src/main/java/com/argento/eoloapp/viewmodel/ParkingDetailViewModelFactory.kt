package com.argento.eoloapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.argento.eoloapp.session.SessionManager

class ParkingDetailViewModelFactory(
    private val sessionManager: SessionManager,
    private val parkingId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ParkingDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ParkingDetailViewModel(sessionManager, parkingId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}