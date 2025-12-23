package com.argento.eoloapp.data

data class CreateReservationRequest(
    val placas: String,
    val numEco: String,
    val tel: String,
    val tipo: String,
    val cat: String,
    val tarifa: String,
    val caja1: String,
    val caja2: String,
    val estacionamiento: String
)
