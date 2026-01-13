package com.argento.eoloapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argento.eoloapp.data.AssignPinRequest
import com.argento.eoloapp.repository.UserRepository
import com.argento.eoloapp.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RestorePinState {
    object Idle : RestorePinState()
    object Loading : RestorePinState()
    data class Success(val message: String) : RestorePinState()
    data class Error(val message: String) : RestorePinState()
}

class RestorePinViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _restorePinState = MutableStateFlow<RestorePinState>(RestorePinState.Idle)
    val restorePinState: StateFlow<RestorePinState> = _restorePinState

    fun assignPin(pin: String) {
        viewModelScope.launch {
            _restorePinState.value = RestorePinState.Loading
            try {
                val token = sessionManager.getAuthToken()
                if (token != null) {
                    val response = userRepository.assignPin("Bearer $token", AssignPinRequest(pin))
                    if (response.status == "success") {
                        _restorePinState.value = RestorePinState.Success("PIN asignado correctamente")
                    } else {
                        _restorePinState.value = RestorePinState.Error("Error al asignar el PIN")
                    }
                } else {
                    _restorePinState.value = RestorePinState.Error("Usuario no autenticado")
                }
            } catch (e: Exception) {
                _restorePinState.value = RestorePinState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}
