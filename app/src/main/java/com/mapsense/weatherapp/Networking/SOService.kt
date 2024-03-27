package com.mapsense.weatherapp.Networking

import com.mapsense.weatherapp.Model.LatLong.LatLongRes
import com.mapsense.weatherapp.Model.Weather.WeatherRes
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface SOService {

    @GET
    fun getlatlong(@Url url: String): Call<LatLongRes>

    @GET
    fun getWeather(@Url url: String) : Call<WeatherRes>
}