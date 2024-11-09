package com.redderi.bookreader.network

import com.redderi.bookreader.model.ApiResponse
import com.redderi.bookreader.model.LoginRequest
import com.redderi.bookreader.model.RegistrationRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/users/login")
    fun login(@Body request: LoginRequest): Call<ApiResponse>

    @POST("api/users/addNewUser")
    fun register(@Body request: RegistrationRequest): Call<ApiResponse>
}