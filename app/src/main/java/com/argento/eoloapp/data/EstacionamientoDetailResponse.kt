package com.argento.eoloapp.data

import com.google.gson.annotations.SerializedName

data class EstacionamientoDetailResponse(
    val status: String,
    val response: EstacionamientoDetailData
)

data class EstacionamientoDetailData(
    val Estacionamiento: EstacionamientoInfo,
    val Reservas: List<Reserva>,
    val Total: Double,
    val ReservaTotal: Double,
    val ServicioTotal: Double,
    val R_EfectivoTotal: Double,
    val R_TerminalTotal: Double,
    val R_CreditoTotal: Double,
    val S_EfectivoTotal: Double,
    val S_TerminalTotal: Double,
    val S_CreditoTotal: Double,
    val Inventario: Int
)

data class EstacionamientoInfo(
    val nombre: String,
    val _id: String
)

data class Reserva(
    @SerializedName("_id") val id: String,
    val ID2: String?,
    @SerializedName("Created Date") val createdDate: Long,
    val tipoVehiculo: String?,
    val placaVehiculo: String?,
    val MontoTotal: Double,
    val Estatus: String?,
    val MetodoPago1: String?
)