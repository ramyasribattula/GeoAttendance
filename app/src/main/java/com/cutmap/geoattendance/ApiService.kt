package com.cutmap.geoattendance

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("api/v2/product/{barcode}.json?fields=product_name,brands,image_url,nutriscore_grade,quantity")
    fun getProduct(@Path("barcode") barcode: String): Call<ProductResponse>

    companion object {
        private const val BASE_URL = "https://world.openfoodfacts.org/"

        val instance: ApiService by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}
