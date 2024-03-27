package com.mapsense.weatherapp.Model.Weather

data class Main(
    var temp: Double,
    var feels_like: Double,
    var pressure: Int,
    var humidity: Double,
    var sea_level: Double,
    var grnd_level: Double
)