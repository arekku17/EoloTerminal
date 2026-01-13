package com.argento.eoloapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.argento.eoloapp.session.SessionManager

class PaymentMethodViewModelFactory(private val sessionManager: SessionManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentMethodViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PaymentMethodViewModel(sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
