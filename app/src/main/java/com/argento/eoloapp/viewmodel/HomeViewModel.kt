package com.argento.eoloapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argento.eoloapp.api.RetrofitInstance
import com.argento.eoloapp.data.Estacionamiento
import com.argento.eoloapp.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _estacionamientosState = MutableStateFlow<EstacionamientosState>(EstacionamientosState.Loading)
    val estacionamientosState: StateFlow<EstacionamientosState> = _estacionamientosState

    init {
        fetchEstacionamientos()
    }

    fun fetchEstacionamientos() {
        viewModelScope.launch {
            _estacionamientosState.value = EstacionamientosState.Loading
            try {
                val token = sessionManager.getAuthToken()
                if (token != null) {
                    val bearerToken = "Bearer $token"
                    val response = RetrofitInstance.api.getEstacionamientos(bearerToken)
                    if (response.status == "success") {
                        _estacionamientosState.value = EstacionamientosState.Success(response.response.estacionamientos)
                    } else {
                        _estacionamientosState.value = EstacionamientosState.Error("Error fetching data")
                    }
                } else {
                    _estacionamientosState.value = EstacionamientosState.Error("No token found")
                }
            } catch (e: Exception) {
                _estacionamientosState.value = EstacionamientosState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class EstacionamientosState {
    object Loading : EstacionamientosState()
    data class Success(val estacionamientos: List<Estacionamiento>) : EstacionamientosState()
    data class Error(val message: String) : EstacionamientosState()
}