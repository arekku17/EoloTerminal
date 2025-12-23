package com.argento.eoloapp.data

import com.google.gson.annotations.SerializedName

data class TarifasResponse(
    val status: String,
    val response: TarifasList
)

data class TarifasList(
    @SerializedName("Tarifas")
    val tarifas: List<Tarifa>
)

data class Tarifa(
    @SerializedName("_id")
    val id: String,
    @SerializedName("Nombre")
    val nombre: String?,
    @SerializedName("Tipo Vehiculo")
    val tipoVehiculo: String?,
    @SerializedName("List Tipos Vehiculo")
    val listTiposVehiculo: List<String>?,
    @SerializedName("Frecuencia")
    val frecuencia: String?,
    @SerializedName("Logica Tarifa")
    val logicaTarifa: String?,
    @SerializedName("Tarifa")
    val tarifa: Double?,
    @SerializedName("Es Default")
    val esDefault: Boolean?
)