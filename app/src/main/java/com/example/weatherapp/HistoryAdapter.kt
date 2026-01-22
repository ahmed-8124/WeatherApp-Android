package com.example.weatherapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(
    private var historyList: MutableList<SearchHistory>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCity: TextView = view.findViewById(R.id.tvHistoryCity)
        val tvStats: TextView = view.findViewById(R.id.tvHistoryStats)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = historyList[position]
        holder.tvCity.text = item.cityName
        holder.tvStats.text = "${item.temp} | Hum: ${item.humidity}"

        holder.itemView.setOnClickListener {
            onItemClick(item.cityName)
        }
    }

    override fun getItemCount() = historyList.size

    fun updateData(newList: List<SearchHistory>) {
        historyList.clear()
        historyList.addAll(newList)
        notifyDataSetChanged()
    }
}