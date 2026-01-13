package com.argento.eoloapp.repository

import com.argento.eoloapp.api.ApiService
import com.argento.eoloapp.data.LoginRequest
import com.argento.eoloapp.data.SendOTPRequest
import com.argento.eoloapp.data.SmsVerifyRequest

class AuthRepository(private val apiService: ApiService) {

    suspend fun login(phone: String, pin: String) = apiService.login(LoginRequest(phone, pin))

    suspend fun sendOtp(phone: String) = apiService.sendOTP(SendOTPRequest(phone))

    suspend fun verifySmsCode(phone: String, code: String) =
        apiService.verifySmsCode(SmsVerifyRequest(phone, code))
}
