package com.mapsense.weatherapp.Model.Weather

data class Weather(
    var id: Int,
    var main: String? = null,
    var description: String? = null,
    var icon: String? = null
)

