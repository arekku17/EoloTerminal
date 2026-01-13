package com.argento.eoloapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.argento.eoloapp.api.RetrofitInstance
import com.argento.eoloapp.repository.AuthRepository
import com.argento.eoloapp.session.SessionManager

class LoginViewModelFactory(private val sessionManager: SessionManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            val authRepository = AuthRepository(RetrofitInstance.api)
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(authRepository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}