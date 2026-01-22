package com.example.weatherapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MainActivity : AppCompatActivity() {
    private val API_KEY = BuildConfig.API_KEY


    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var db: AppDatabase
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var gestureDetector: GestureDetector
    private lateinit var tvCityName: TextView
    private lateinit var tvTemperature: TextView
    private lateinit var tvHumidity: TextView
    private lateinit var tvWindSpeed: TextView
    private lateinit var tvAQI: TextView
    private lateinit var loader: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        db = AppDatabase.getDatabase(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        tvCityName = findViewById(R.id.tvCityName)
        tvTemperature = findViewById(R.id.tvTemperature)
        tvHumidity = findViewById(R.id.tvHumidity)
        tvWindSpeed = findViewById(R.id.tvWindSpeed)
        tvAQI = findViewById(R.id.tvAQI)
        loader = findViewById(R.id.progressBar)

        val etCity = findViewById<AutoCompleteTextView>(R.id.search)
        val btnGetWeather = findViewById<Button>(R.id.Search_button)
        val rvHistory = findViewById<RecyclerView>(R.id.rvHistory)
                // sliding gesture feature

       gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vx: Float, vy: Float): Boolean {
                // Detect Swipe from Right to Left
                val distanceX = (e1?.x ?: 0f) - e2.x
                if (distanceX > 100 && Math.abs(vx) > 100) {
                    val lastCity = tvCityName.text.toString().trim()
                    if (lastCity.isNotEmpty() && lastCity != "Search for a city") {
                        fetchAndOpenDetails(lastCity)
                        // Add the slide animation
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                    return true
                }
                return false
            }
        })
        historyAdapter = HistoryAdapter(mutableListOf()) { clickedCity ->
            fetchAndOpenDetails(clickedCity)
        }
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = historyAdapter

        lifecycleScope.launch {
            db.historyDAO().getAllHistory().collectLatest { list ->
                historyAdapter.updateData(list)
            }
        }

        // 4. Setup AutoComplete Search
        setupCitySearch(etCity)

        // 5. Button Logic: Manual City Search
        btnGetWeather.setOnClickListener {
            val cityName = etCity.text.toString().trim()
            if (cityName.isNotEmpty()) {
                getWeatherData(cityName, isCurrentLocation = false)
            } else {
                Toast.makeText(this, "Please enter a city", Toast.LENGTH_SHORT).show()
            }
        }

        //  Automatic Location Trigger
        checkLocationPermission()
    }
    private fun checkLocationPermission() {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                getCurrentLocationWeather()
            } else {
                tvCityName.text = "Search for a city"
            }
        }

        requestPermissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    private fun getCurrentLocationWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                fetchWeatherByCoords(it.latitude, it.longitude)
            } ?: run {
                tvCityName.text = "Search for a city"
            }
        }
    }
    // converting long/lad to city name
    private fun fetchWeatherByCoords(lat: Double, lon: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            val cityName = addresses?.get(0)?.locality ?: "Unknown Location"
            getWeatherData(cityName, isCurrentLocation = true)
        } catch (e: Exception) {
            Log.e("GEO_ERROR", "Geocoder failed: ${e.message}")
        }
    }

    private fun getWeatherData(city: String, isCurrentLocation: Boolean) {
        hideKeyboard()
        lifecycleScope.launch {
            loader.visibility = View.VISIBLE
            val startTime = System.currentTimeMillis()
            val cachedCity = withContext(Dispatchers.IO) {
                db.historyDAO().getCityByName(city)
            }
            val dbTime = System.currentTimeMillis() - startTime
            Log.d("WEATHER_DEBUG", "DB Fetch took: ${dbTime}ms")
            // DB check
            if (cachedCity != null) {
                val isFresh = System.currentTimeMillis() - cachedCity.timestamp < 30 * 60 * 1000

                if (isFresh) {
                    tvCityName.text =
                        if (isCurrentLocation) " ${cachedCity.cityName}" else cachedCity.cityName
                    tvTemperature.text = cachedCity.temp
                    tvHumidity.text = "Humidity: ${cachedCity.humidity}"
                    tvWindSpeed.text = "Wind: ${cachedCity.wind}"
                    loader.visibility = View.GONE
                    return@launch
                }

            } else {
                // api call for fetching the daata
                try {
                    val weather = RetrofitClient.api.getWeather(city, API_KEY)
                    val pollution = async {
                        RetrofitClient.api.getAirPollution(
                            weather.coord.lat,
                            weather.coord.lon,
                            API_KEY
                        )
                    }.await()

                    tvCityName.text = if (isCurrentLocation) " ${weather.name}" else weather.name

                    val currentTemp = "${weather.main.temp.toInt()}°C"
                    tvTemperature.text = currentTemp
                    tvHumidity.text = "Humidity: ${weather.main.humidity}%"
                    tvWindSpeed.text = "Wind: ${weather.wind.speed} m/s"
                    tvAQI.text = "Air Quality: ${getAQIText(pollution.list[0].main.aqi)}"


                    updateHistory(
                        weather.name,
                        currentTemp,
                        "${weather.main.humidity}%",
                        "${weather.wind.speed} m/s"
                    )

                }catch (e: retrofit2.HttpException) {
                    when (e.code()) {
                        429 -> {
                            Toast.makeText(this@MainActivity, "API Limit reached! Please wait a minute.", Toast.LENGTH_LONG).show()
                        }
                        else->{
                            Toast.makeText(this@MainActivity, "Server Error: ${e.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Weather_Error", e.message.toString())
                    Toast.makeText(this@MainActivity, "City not found", Toast.LENGTH_SHORT).show()
                } finally {
                    loader.visibility = View.GONE
                }

            }
        }
    }

    private fun updateHistory(name: String, temp: String, hum: String, wind: String) {
        lifecycleScope.launch {
            val newEntry = SearchHistory(name, temp, hum, wind, System.currentTimeMillis())
            db.historyDAO().insertSearch(newEntry)
            val updateList = db.historyDAO().getAllHistory().first()
            withContext(Dispatchers.Main){
                historyAdapter.updateData(updateList)
            }
        }
    }
                    // print detail on the 2 screen
    private fun fetchAndOpenDetails(city: String) {
        lifecycleScope.launch {
            loader.visibility = View.VISIBLE
            try {
                val weatherResponse = RetrofitClient.api.getWeather(city, API_KEY)
                val forecastDeferred = async { RetrofitClient.api.getForecast(city, API_KEY) }
                val pollutionDeferred = async {
                    RetrofitClient.api.getAirPollution(weatherResponse.coord.lat, weatherResponse.coord.lon, API_KEY)
                }

                val intent = Intent(this@MainActivity, DetailActivity::class.java).apply {
                    putExtra("CITY_NAME", weatherResponse.name)
                    putExtra("TEMP", "${weatherResponse.main.temp.toInt()}°C")
                    putExtra("HUMIDITY", "${weatherResponse.main.humidity}%")
                    putExtra("WIND", "${weatherResponse.wind.speed} m/s")
                    putExtra("AQI", pollutionDeferred.await().list[0].main.aqi)
                    putExtra("FORECAST_JSON", Gson().toJson(forecastDeferred.await().list))
                }
                startActivity(intent)

            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error loading details", Toast.LENGTH_SHORT).show()
            } finally {
                loader.visibility = View.GONE
            }
        }
    }

    private fun setupCitySearch(autoCompleteTextView: AutoCompleteTextView) {
        try {
            val jsonString = assets.open("pakistan_cities.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<String>>() {}.type
            val cities: List<String> = Gson().fromJson(jsonString, listType)
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cities)
            autoCompleteTextView.setAdapter(adapter)
            autoCompleteTextView.threshold = 1
        } catch (e: Exception) {
            Log.e("JSON_ERROR", "Error: ${e.message}")
        }
    }
    /*override fun onTouchEvent(event: MotionEvent): Boolean {
            return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }*/
    private fun getAQIText(aqi: Int): String = when (aqi) {
        1 -> "Excellent"
        2 -> "Good"
        3 -> "Fair"
        4 -> "Poor"
        5 -> "Hazardous"
        else -> "Unknown"
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}