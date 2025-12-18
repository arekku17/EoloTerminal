package com.argento.eoloapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argento.eoloapp.api.RetrofitInstance
import com.argento.eoloapp.data.EstacionamientoDetailData
import com.argento.eoloapp.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ParkingDetailViewModel(
    private val sessionManager: SessionManager,
    private val parkingId: String
) : ViewModel() {

    private val _detailState = MutableStateFlow<ParkingDetailState>(ParkingDetailState.Loading)
    val detailState: StateFlow<ParkingDetailState> = _detailState

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        fetchParkingDetail(isRefresh = false)
    }

    fun fetchParkingDetail(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _isRefreshing.value = true
            } else {
                _detailState.value = ParkingDetailState.Loading
            }
            
            try {
                val token = sessionManager.getAuthToken()
                if (token != null) {
                    val bearerToken = "Bearer $token"
                    val response = RetrofitInstance.api.getEstacionamientoDetail(bearerToken, parkingId)
                    if (response.status == "success") {
                        _detailState.value = ParkingDetailState.Success(response.response)
                    } else {
                        // Only update error state if we are not refreshing, to avoid hiding current data
                        if (!isRefresh) {
                            _detailState.value = ParkingDetailState.Error("Error fetching data")
                        }
                        // Optionally handle refresh error (e.g., show toast)
                    }
                } else {
                    if (!isRefresh) {
                        _detailState.value = ParkingDetailState.Error("No token found")
                    }
                }
            } catch (e: Exception) {
                if (!isRefresh) {
                    _detailState.value = ParkingDetailState.Error(e.message ?: "Unknown error")
                }
            } finally {
                if (isRefresh) {
                    _isRefreshing.value = false
                }
            }
        }
    }
}

sealed class ParkingDetailState {
    object Loading : ParkingDetailState()
    data class Success(val data: EstacionamientoDetailData) : ParkingDetailState()
    data class Error(val message: String) : ParkingDetailState()
}