package com.argento.eoloapp.data

import com.google.gson.annotations.SerializedName

data class SmsVerifyRequest(
    val phone: String,
    val code: String
)

data class SmsVerifyResponse(
    val status: String,
    val response: SmsVerifyResponseData
)

data class SmsVerifyResponseData(
    val verify: Boolean?,
    val token: String?,
    @SerializedName("user_id") val userId: String?,
    val expires: Long?
)
