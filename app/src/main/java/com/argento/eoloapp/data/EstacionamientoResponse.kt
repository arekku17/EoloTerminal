package com.argento.eoloapp.data

import com.google.gson.annotations.SerializedName

data class EstacionamientoResponse(
    val status: String,
    val response: EstacionamientosList
)

data class EstacionamientosList(
    val estacionamientos: List<Estacionamiento>
)

data class Estacionamiento(
    @SerializedName("_id") val id: String,
    val nombre: String,
    val razon_social: String?,
    val ciudad: String?,
    val estado: String?,
    val Activo: Boolean?,
    val timeZoneId: String?
    // Add other fields if necessary, but these are enough for the list
)