package com.argento.eoloapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argento.eoloapp.api.RetrofitInstance
import com.argento.eoloapp.data.CreateReservationRequest
import com.argento.eoloapp.data.EstacionamientoDetailData
import com.argento.eoloapp.data.Reserva
import com.argento.eoloapp.data.Tarifa
import com.argento.eoloapp.session.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    private val _tariffsState = MutableStateFlow<List<Tarifa>>(emptyList())
    val tariffsState: StateFlow<List<Tarifa>> = _tariffsState.asStateFlow()

    private val _reservationCreationState = MutableStateFlow<ReservationCreationState>(ReservationCreationState.Idle)
    val reservationCreationState: StateFlow<ReservationCreationState> = _reservationCreationState.asStateFlow()

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    private var searchJob: Job? = null

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

    fun fetchTarifas(tipo: String, categoria: String) {
        viewModelScope.launch {
            try {
                val token = sessionManager.getAuthToken()
                if (token != null) {
                    val bearerToken = "Bearer $token"
                    val response = RetrofitInstance.api.getTarifas(bearerToken, parkingId, tipo, categoria)
                    if (response.status == "success") {
                        _tariffsState.value = response.response.tarifas
                    }
                }
            } catch (e: Exception) {
                _tariffsState.value = emptyList()
            }
        }
    }

    fun createReservation(
        placas: String,
        numEco: String,
        tel: String,
        tipo: String,
        cat: String,
        tarifaId: String,
        caja1: String,
        caja2: String
    ) {
        viewModelScope.launch {
            _reservationCreationState.value = ReservationCreationState.Loading
            try {
                val token = sessionManager.getAuthToken()
                if (token != null) {
                    val bearerToken = "Bearer $token"
                    val request = CreateReservationRequest(
                        placas = placas,
                        numEco = numEco,
                        tel = tel,
                        tipo = tipo,
                        cat = cat,
                        tarifa = tarifaId,
                        caja1 = caja1,
                        caja2 = caja2,
                        estacionamiento = parkingId
                    )
                    val response = RetrofitInstance.api.createReservation(bearerToken, request)
                    if (response.status == "success") {
                        _reservationCreationState.value = ReservationCreationState.Success
                        fetchParkingDetail(isRefresh = true) // Refetch data
                    } else {
                        _reservationCreationState.value = ReservationCreationState.Error("Error: ${response.status}")
                    }
                } else {
                    _reservationCreationState.value = ReservationCreationState.Error("No token found")
                }
            } catch (e: Exception) {
                _reservationCreationState.value = ReservationCreationState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetReservationCreationState() {
        _reservationCreationState.value = ReservationCreationState.Idle
    }

    fun searchMovimientos(query: String) {
        searchJob?.cancel()
        
        if (query.isBlank()) {
            _searchState.value = SearchState.Idle
            return
        }

        searchJob = viewModelScope.launch {
            delay(500) // Debounce
            _searchState.value = SearchState.Loading
            try {
                val token = sessionManager.getAuthToken()
                if (token != null) {
                    val bearerToken = "Bearer $token"
                    val response = RetrofitInstance.api.searchMovimientos(bearerToken, parkingId, query)
                    if (response.status == "success") {
                        _searchState.value = SearchState.Success(response.response.movimientos)
                    } else {
                        _searchState.value = SearchState.Error("Error searching movements")
                    }
                }
            } catch (e: Exception) {
                _searchState.value = SearchState.Error(e.message ?: "Unknown search error")
            }
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _searchState.value = SearchState.Idle
    }
}

sealed class ParkingDetailState {
    object Loading : ParkingDetailState()
    data class Success(val data: EstacionamientoDetailData) : ParkingDetailState()
    data class Error(val message: String) : ParkingDetailState()
}

sealed class ReservationCreationState {
    object Idle : ReservationCreationState()
    object Loading : ReservationCreationState()
    object Success : ReservationCreationState()
    data class Error(val message: String) : ReservationCreationState()
}

sealed class SearchState {
    object Idle : SearchState()
    object Loading : SearchState()
    data class Success(val results: List<Reserva>) : SearchState()
    data class Error(val message: String) : SearchState()
}
