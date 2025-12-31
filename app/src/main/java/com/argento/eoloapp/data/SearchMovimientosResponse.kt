package com.argento.eoloapp.data

data class SearchMovimientosResponse(
    val status: String,
    val response: SearchMovimientosData
)

data class SearchMovimientosData(
    val count: Int,
    val movimientos: List<Reserva>
)
