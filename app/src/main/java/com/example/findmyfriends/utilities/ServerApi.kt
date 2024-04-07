package com.example.findmyfriends.utilities

import com.example.findmyfriends.data.model.DataUser
import com.google.gson.GsonBuilder
import com.google.gson.internal.LinkedTreeMap
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface LocationApi {
    @POST("/user/{uid}")

    suspend fun updateUser(@Path("uid")userId: String, @Query("lat")latitude: String, @Query("long")longitude: String) : Response<Any>

    @GET("/group/{gid}")
    suspend fun getGroupUpdate(@Path("gid")groupId: String): Response<LinkedTreeMap<String, LinkedTreeMap<String, Any>>>
}

object ApiRetrofitHelper {

    const val BASEURL = "https://marcodl.pythonanywhere.com"
    var gson = GsonBuilder()
        .setLenient()
        .create()
    fun getInstance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASEURL)
            .addConverterFactory(GsonConverterFactory.create(gson)) // JSON converter to Kotlin object
            .build()
    }

}