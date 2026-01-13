package com.argento.eoloapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argento.eoloapp.repository.AuthRepository
import com.argento.eoloapp.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(phone: String, pin: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = authRepository.login(phone, pin)
                if (response.status == "success" && response.response != null) {
                    sessionManager.saveAuthToken(response.response.token)
                    _loginState.value = LoginState.Success
                } else {
                    _loginState.value = LoginState.Error("Credenciales Incorrectas")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Credenciales Incorrectas")
            }
        }
    }

    fun sendOtp(phone: String) {
        viewModelScope.launch {
            try {
                authRepository.sendOtp(phone)
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    fun verifySmsCode(phone: String, code: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = authRepository.verifySmsCode(phone, code)
                if (response.status == "success" && response.response.token != null) {
                    sessionManager.saveAuthToken(response.response.token)
                    _loginState.value = LoginState.Success
                } else {
                    _loginState.value = LoginState.Error("Código incorrecto")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Error de verificación")
            }
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}
