package com.example.weatherapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Recommend adding Glide to your build.gradle for icons

class ForecastAdapter(private val forecastList: List<ForecastItem>) :
    RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    class ForecastViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTime: TextView = view.findViewById(R.id.tvForecastTime)
        val tvTemp: TextView = view.findViewById(R.id.tvForecastTemp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forecast, parent, false)
        return ForecastViewHolder(view)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val item = forecastList[position]


        val fullDate = item.dtTxt
        val timeOnly = fullDate.substring(11, 16)
        holder.tvTime.text = timeOnly

        // 2. Set Temperature
        holder.tvTemp.text = "${item.main.temp.toInt()}°C"

        // 3. Load Icon
        val iconCode = item.weather[0].icon
        val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"

        Glide.with(holder.itemView.context)
            .load(iconUrl)

    }

    override fun getItemCount(): Int = forecastList.size
}