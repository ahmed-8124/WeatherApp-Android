package com.example.weatherapp

import com.google.gson.annotations.SerializedName

// --- CURRENT WEATHER ---
data class WeatherResponse(
    val main: MainData,
    val wind: WindData,
    val name: String,
    val coord: Coordinates,
    val weather: List<WeatherDescription>
)

data class MainData(
    val temp: Double,
    val humidity: Int,
    val pressure: Int
)

data class WindData(
    val speed: Double
)

data class Coordinates(
    val lat: Double,
    val lon: Double
)

data class AirPollutionResponse(
    val list: List<PollutionData>
)

data class PollutionData(
    val main: PollutionMain,
    val components: Map<String, Double>
)

data class PollutionMain(
    val aqi: Int
)


data class ForecastResponse(
    val list: List<ForecastItem>,
    val city: CityInfo
)

data class ForecastItem(
    @SerializedName("dt_txt") val dtTxt: String,
    val main: MainData,
    val weather: List<WeatherDescription>
)

data class WeatherDescription(
    val description: String,
    val icon: String
)

data class CityInfo(
    val name: String,
    val country: String
)