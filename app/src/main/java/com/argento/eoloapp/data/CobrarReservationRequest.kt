package com.argento.eoloapp.data

data class CobrarReservationRequest(
    val monto_saldo: Double,
    val wallet: String,
    val montoreserva: Double,
    val metodopago1: String, //Terminal/Efectivo
    val metodopago2: String?, //Terminal/Efectivo
    val tipotarjeta: String,
    val monto1: Double,
    val monto2: Double,
    val reserva: String
)
