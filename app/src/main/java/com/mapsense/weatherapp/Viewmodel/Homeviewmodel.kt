package com.mapsense.weatherapp.Viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.lotteryapp.utility.interfaces.onApiCall
import com.mapsense.weatherapp.Model.LatLong.LocationModel
import com.mapsense.weatherapp.Model.LatLong.LatLongRes
import com.mapsense.weatherapp.Model.Weather.WeatherRes
import com.mapsense.weatherapp.Networking.ApiUtilsMap
import com.mapsense.weatherapp.Networking.ApiUtilsWeather
import com.mapsense.weatherapp.R
import com.mapsense.weatherapp.Utility.Utils
import com.mapsense.weatherapp.Utility.Utils.getNointernet
import retrofit2.Call
import retrofit2.Response

class Homeviewmodel(application: Application, apiCall: onApiCall) : AndroidViewModel(application) {

    val apiCall: onApiCall = apiCall
    val noInternetError = getNointernet(application)
    val locationResults: MutableLiveData<LocationModel> = MutableLiveData<LocationModel>()
    val weatherResults: MutableLiveData<WeatherRes> = MutableLiveData<WeatherRes>()

    fun getLatLongFromPlaceId(context: Context, place_id: String) {
        if (Utils.isNetworkConnected(getApplication())) {
            val URL =
                "place/details/json?place_id=$place_id&fields=geometry&key=" + context.resources.getString(
                    R.string.google_maps_key
                )

            ApiUtilsMap.getPlaceInstance().getlatlong(URL)
                .enqueue(object : retrofit2.Callback<LatLongRes> {
                    override fun onResponse(
                        call: Call<LatLongRes>,
                        response: Response<LatLongRes>
                    ) {
                        if (response.body() != null) {
                            locationResults.postValue(response.body()!!.result.geometry.location)
                        } else {
                            apiCall.onError("LatLongResp", "Null response")
                        }
                        Log.d("response", response.body()!!.result.geometry.location.lng.toString())
                    }

                    override fun onFailure(call: Call<LatLongRes>, t: Throwable) {
                        Log.d("response", t.message!!)
                    }
                })
        } else {
            apiCall.onError("LatLongResp", noInternetError)
        }
    }

    fun getWeatherFromLatLong(lat: String, lng: String) {
        if (Utils.isNetworkConnected(getApplication())) {
            val URL = "data/2.5/weather?lat=$lat&lon=$lng&appid=e53301e27efa0b66d05045d91b2742d3"
            ApiUtilsWeather.getWaetherInstance().getWeather(URL)
                .enqueue(object : retrofit2.Callback<WeatherRes> {
                    override fun onResponse(
                        call: Call<WeatherRes>,
                        response: Response<WeatherRes>
                    ) {
                        if (response.body() != null) {
                            weatherResults.postValue(response.body())
                        } else {
                            apiCall.onError("WEATHER", "Null response")
                        }
                    }

                    override fun onFailure(call: Call<WeatherRes>, t: Throwable) {

                    }
                })
        } else {
            apiCall.onError("WeatherResp", noInternetError)
        }
    }
}