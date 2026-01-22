package com.example.weatherapp
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("weather")
    suspend fun getWeather(
        @Query("q") city:String,
        @Query("appid") apikey: String,
        @Query("units") untis: String = "metric"
    ):WeatherResponse

    @GET("forecast")
    suspend fun getForecast(
        @Query("q") city: String,
        @Query("appid") apikey: String,
        @Query("units") untis: String = "metric"
    ): ForecastResponse

    @GET("air_pollution")
    suspend fun getAirPollution(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String
    ): AirPollutionResponse
    @GET("weather")
    fun getWeatherDataByCoords(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apikey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse
}