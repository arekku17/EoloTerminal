package com.argento.eoloapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.argento.eoloapp.api.RetrofitInstance
import com.argento.eoloapp.data.ReservationDetailWrapper
import com.argento.eoloapp.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MovementDetailViewModel(
    private val sessionManager: SessionManager,
    private val reservationId: String,
    private val parkingId: String
) : ViewModel() {
    private val _uiState = MutableStateFlow<MovementDetailState>(MovementDetailState.Loading)
    val uiState: StateFlow<MovementDetailState> = _uiState.asStateFlow()

    init {
        fetchReservationDetail()
    }

    fun fetchReservationDetail() {
        viewModelScope.launch {
            _uiState.value = MovementDetailState.Loading
            try {
                val token = sessionManager.getAuthToken()
                if (token != null) {
                    val bearerToken = "Bearer $token"
                    val response = RetrofitInstance.api.getReservationDetail(bearerToken, reservationId, parkingId)
                    if (response.status == "success") {
                        _uiState.value = MovementDetailState.Success(response.response)
                    } else {
                        _uiState.value = MovementDetailState.Error("Error: ${response.status}")
                    }
                } else {
                    _uiState.value = MovementDetailState.Error("No authentication token")
                }
            } catch (e: Exception) {
                _uiState.value = MovementDetailState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class MovementDetailState {
    object Loading : MovementDetailState()
    data class Success(val reserva: ReservationDetailWrapper) : MovementDetailState()
    data class Error(val message: String) : MovementDetailState()
}

class MovementDetailViewModelFactory(
    private val sessionManager: SessionManager,
    private val reservationId: String,
    private val parkingId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovementDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MovementDetailViewModel(sessionManager, reservationId, parkingId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
