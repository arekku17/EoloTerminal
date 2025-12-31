package com.argento.eoloapp.api

import com.argento.eoloapp.data.BasicResponse
import com.argento.eoloapp.data.CreateReservationRequest
import com.argento.eoloapp.data.EstacionamientoDetailResponse
import com.argento.eoloapp.data.EstacionamientoResponse
import com.argento.eoloapp.data.LoginRequest
import com.argento.eoloapp.data.LoginResponse
import com.argento.eoloapp.data.ReservationDetailResponse
import com.argento.eoloapp.data.SearchMovimientosResponse
import com.argento.eoloapp.data.TarifasResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("apk_login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("apk_get_estacionamientos")
    suspend fun getEstacionamientos(@Header("Authorization") token: String): EstacionamientoResponse

    @GET("apk_get_estacionamiento")
    suspend fun getEstacionamientoDetail(
        @Header("Authorization") token: String,
        @Query("Estacionamiento") parkingId: String
    ): EstacionamientoDetailResponse

    @GET("apk_get_tarifas")
    suspend fun getTarifas(
        @Header("Authorization") token: String,
        @Query("Estacionamiento") parkingId: String,
        @Query("Tipo") tipo: String,
        @Query("Categoria") categoria: String
    ): TarifasResponse

    @POST("apk_post_reserva")
    suspend fun createReservation(
        @Header("Authorization") token: String,
        @Body request: CreateReservationRequest
    ): BasicResponse

    @GET("apk_get_reserva")
    suspend fun getReservationDetail(
        @Header("Authorization") token: String,
        @Query("reserva") reservationId: String
    ): ReservationDetailResponse

    @GET("apk_search_movimientos")
    suspend fun searchMovimientos(
        @Header("Authorization") token: String,
        @Query("Estacionamiento") parkingId: String,
        @Query("search") query: String,
    ): SearchMovimientosResponse
}
