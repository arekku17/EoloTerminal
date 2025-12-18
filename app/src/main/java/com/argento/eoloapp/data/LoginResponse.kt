package com.argento.eoloapp.data

data class LoginResponse(
    val status: String,
    val response: TokenResponse?
)

data class TokenResponse(
    val token: String,
    val user_id: String,
    val expires: Long
)