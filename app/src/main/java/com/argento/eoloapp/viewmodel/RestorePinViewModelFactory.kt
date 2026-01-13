package com.argento.eoloapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.argento.eoloapp.api.ApiClient
import com.argento.eoloapp.repository.UserRepository
import com.argento.eoloapp.session.SessionManager

class RestorePinViewModelFactory(private val sessionManager: SessionManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RestorePinViewModel::class.java)) {
            val apiService = ApiClient.apiService
            val userRepository = UserRepository(apiService)
            @Suppress("UNCHECKED_CAST")
            return RestorePinViewModel(userRepository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
