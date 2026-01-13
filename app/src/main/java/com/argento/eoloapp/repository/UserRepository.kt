package com.argento.eoloapp.repository

import com.argento.eoloapp.api.ApiService
import com.argento.eoloapp.data.AssignPinRequest
import com.argento.eoloapp.data.BasicResponse

class UserRepository(private val apiService: ApiService) {

    suspend fun assignPin(token: String, request: AssignPinRequest): BasicResponse {
        return apiService.assignPin(token, request)
    }
}
