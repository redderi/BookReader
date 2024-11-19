package com.redderi.bookreader.network

import com.redderi.bookreader.model.ApiResponse
import com.redderi.bookreader.model.Book
import com.redderi.bookreader.model.BookIdRequest
import com.redderi.bookreader.model.LoginRequest
import com.redderi.bookreader.model.PasswordChangeRequest
import com.redderi.bookreader.model.RegistrationRequest
import com.redderi.bookreader.model.UsernameChangeRequest
import com.redderi.bookreader.model.Quote
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @POST("api/users/login")
    fun login(@Body request: LoginRequest): Call<ApiResponse>

    @POST("api/users/addNewUser")
    fun register(@Body request: RegistrationRequest): Call<ApiResponse>

    @GET("api/books")
    fun getAllBooks(): Call<List<Book>>

    @GET("/api/books/download/{id}")
    fun downloadBook(@Path("id") bookId: Long): Call<ResponseBody>

    @GET("api/users/{username}/books")
    fun getUserBooks(@Path("username") username: String): Call<List<Book>>

    @POST("api/users/{username}/books")
    fun addBookToUser(
        @Path("username") username: String,
        @Body bookIdRequest: BookIdRequest
    ): Call<ApiResponse>

    @PUT("api/users/changeUsername/{username}")
    fun changeUsername(
        @Path("username") username: String,
        @Body request: UsernameChangeRequest
    ): Call<ApiResponse>

    @PUT("/changePassword/{username}")
    fun changePassword(
        @Path("username") username: String,
        @Body request: PasswordChangeRequest
    ): Call<ApiResponse>

    @DELETE("api/users/{username}")
    fun deleteUser(
        @Path("username") username: String
    ): Call<ApiResponse>

    @POST("api/users/{username}/quotes")
    fun addQuoteToUser(
        @Path("username") username: String,
        @Body quote: Quote
    ): Call<ApiResponse>

    @DELETE("api/users/{username}/quotes/{index}")
    fun removeQuoteFromUser(
        @Path("username") username: String,
        @Path("index") index: Int
    ): Call<ApiResponse>

    @PUT("api/users/{username}/quotes/{index}")
    fun updateQuoteOfUser(
        @Path("username") username: String,
        @Path("index") index: Int,
        @Body newQuote: Quote
    ): Call<ApiResponse>

    @GET("api/users/{username}/quotes")
    fun getUserQuotes(
        @Path("username") username: String
    ): Call<List<Quote>>

    @DELETE("api/users/{username}/books/{bookId}")
    fun removeBookFromUser(
        @Path("username") username: String,
        @Path("bookId") bookId: Long
    ): Call<ApiResponse>
}