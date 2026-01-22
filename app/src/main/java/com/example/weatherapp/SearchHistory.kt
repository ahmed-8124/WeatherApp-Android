package com.example.weatherapp

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(tableName = "search_history",
    indices = [Index(value = ["cityName"])]
)

data class SearchHistory(
    @PrimaryKey val cityName: String,
    val temp: String,
    val humidity: String,
    val wind: String,
    val timestamp: Long
)