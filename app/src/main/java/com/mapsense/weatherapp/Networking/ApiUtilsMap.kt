package com.mapsense.weatherapp.Networking

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiUtilsMap {

    var BASE_URL_GOOGLE_PLACE = "https://maps.googleapis.com/maps/api/"
    var retrofit: Retrofit? = null

    fun getPlaceInstance(): SOService {
        retrofit = getApiClientForGooglePlace()
        return retrofit!!.create(SOService::class.java)
    }

    private fun getApiClientForGooglePlace(): Retrofit {
        if (retrofit == null) {
            val okHttpClient = OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)

            okHttpClient.interceptors().add(Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(request)
            })
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            okHttpClient.addInterceptor(httpLoggingInterceptor)

            return Retrofit.Builder()
                .baseUrl(BASE_URL_GOOGLE_PLACE)
                .client(okHttpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit as Retrofit
    }


}