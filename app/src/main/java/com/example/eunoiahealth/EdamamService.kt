package com.example.eunoiahealth

import FoodResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface EdamamService {
    @GET("api/food-database/v2/parser")
    fun getFoodInfo(
        @Query("ingr") foodName: String,
        @Query("app_id") appId: String,
        @Query("app_key") apiKey: String
    ): Call<FoodResponse>  // Replace FoodResponse with your data model class
}
