package com.example.wheaterapp

import android.content.Context

class CityManager(private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
    private val keyCities = "cities"

    fun addCity(city: String) {
        val cities = getCities().toMutableSet()
        cities.add(city)
        sharedPreferences.edit().putStringSet(keyCities, cities).apply()
    }

    fun getCities(): Set<String> {
        return sharedPreferences.getStringSet(keyCities, emptySet()) ?: emptySet()
    }

    fun removeCity(city: String) {
        val cities = getCities().toMutableSet()
        cities.remove(city)
        sharedPreferences.edit().putStringSet(keyCities, cities).apply()
    }
}