package com.mapsense.weatherapp.Model.Weather

import java.nio.channels.FileLock


data class WeatherRes(
    val coord: Coord? = null,
    var weather: ArrayList<Weather>? = null,
    var base: String? = null,
    var main: Main? = null,
    var wind: Wind? = null,
    var id: Double,
    var name: String? = null,
)
