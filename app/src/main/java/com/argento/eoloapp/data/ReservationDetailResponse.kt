package com.argento.eoloapp.data

import com.google.gson.annotations.SerializedName

data class ReservationDetailResponse(
    val status: String,
    val response: ReservationDetailWrapper
)

data class ReservationDetailWrapper(
    val reserva: Reserva,
    val vehiculo: Any?, // Keeping generic as it's empty in example
    val user: Any?, // Keeping generic
    val tarifa: Tarifa,
    val estacionamiento: Estacionamiento
)

data class Reserva(
    @SerializedName("_id") val id: String,
    @SerializedName("ID2") val folio: String?,
    @SerializedName("Estatus") val estatus: String?,
    @SerializedName("Created Date") val createdDate: Long,
    @SerializedName("fechaIngreso") val fechaIngreso: Long?,
    @SerializedName("fechaSalida") val fechaSalida: Long?,
    @SerializedName("NombreResponsable") val nombreResponsable: String?,
    @SerializedName("tipoVehiculo") val tipoVehiculo: String?,
    @SerializedName("categoriaVehiculo") val categoriaVehiculo: String?,
    @SerializedName("placaVehiculo") val placaVehiculo: String?,
    @SerializedName("MontoTotal") val MontoTotal: Double?,
    val TarifaJSON: String?,
    @SerializedName("MetodoPago1") val MetodoPago1: String?,
)
