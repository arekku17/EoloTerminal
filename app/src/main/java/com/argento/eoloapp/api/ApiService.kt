package com.argento.eoloapp.api

import com.argento.eoloapp.data.EstacionamientoDetailResponse
import com.argento.eoloapp.data.EstacionamientoResponse
import com.argento.eoloapp.data.LoginRequest
import com.argento.eoloapp.data.LoginResponse
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
}