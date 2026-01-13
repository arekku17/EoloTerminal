package com.argento.eoloapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argento.eoloapp.api.RetrofitInstance
import com.argento.eoloapp.data.CobrarReservationRequest
import com.argento.eoloapp.data.Result
import com.argento.eoloapp.repository.ReservationRepository
import com.argento.eoloapp.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PaymentMethodViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _cobroState = MutableStateFlow<Result<Unit>>(Result.Idle)
    val cobroState: StateFlow<Result<Unit>> = _cobroState

    fun cobrarReservation(request: CobrarReservationRequest) {
        viewModelScope.launch {
            _cobroState.value = Result.Loading
            try {
                val token = sessionManager.getAuthToken()
                if (token != null) {
                    val bearerToken = "Bearer $token"
                    val response = RetrofitInstance.api.cobrarReservation(bearerToken, request)
                    if (response.status == "success") {
                        _cobroState.value = Result.Success(Unit)
                    }
                    }

            } catch (e: Exception) {
                _cobroState.value = Result.Error(e)
            }
        }
    }

    fun resetCobroState() {
        _cobroState.value = Result.Idle
    }
}