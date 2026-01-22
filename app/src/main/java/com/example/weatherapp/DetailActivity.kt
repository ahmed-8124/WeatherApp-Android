package com.example.weatherapp

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }


        val cityName = intent.getStringExtra("CITY_NAME")
        val temp = intent.getStringExtra("TEMP")
        val humidity = intent.getStringExtra("HUMIDITY")
        val wind = intent.getStringExtra("WIND")
        val aqi = intent.getIntExtra("AQI", 1)
        val forecastJson = intent.getStringExtra("FORECAST_JSON")

        findViewById<TextView>(R.id.detCity).text = cityName
        findViewById<TextView>(R.id.detTemp).text = temp
        findViewById<TextView>(R.id.detHum).text = "Humidity: $humidity"
        findViewById<TextView>(R.id.detWind).text = "Wind Speed: $wind"
        findViewById<TextView>(R.id.detAqi).text = "Air Quality: ${getAQIDescription(aqi)}"

        val rvForecast = findViewById<RecyclerView>(R.id.rvDetailForecast)
        val listType = object : TypeToken<List<ForecastItem>>() {}.type
        val forecastList: List<ForecastItem> = Gson().fromJson(forecastJson, listType)

        rvForecast.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvForecast.adapter = ForecastAdapter(forecastList)
    }

    private fun getAQIDescription(aqi: Int): String {
        return when (aqi) {
            1 -> "Excellent"
            2 -> "Good"
            3 -> "Fair"
            4 -> "Poor"
            5 -> "Hazardous"
            else -> "N/A"
        }
    }
}