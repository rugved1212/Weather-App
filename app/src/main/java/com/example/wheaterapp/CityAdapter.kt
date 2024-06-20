package com.example.weatherapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wheaterapp.ApiInterface
import com.example.wheaterapp.R
import com.example.wheaterapp.WeatherApp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CityAdapter(private var cities: List<String>, private var listener: OnItemClickListener) : RecyclerView.Adapter<CityAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(city: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cities, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val city = cities[position]
        holder.bind(city, listener)
        fetchWeatherData(city) { temperature ->
            holder.bindTemperature(temperature)
        }
    }

    override fun getItemCount(): Int {
        return cities.size
    }

    fun updateCities(newCities: List<String>) {
        cities = newCities
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cityNameText : TextView = itemView.findViewById(R.id.city_name)
        private val tempText : TextView = itemView.findViewById(R.id.city_temp)

        fun bind(city: String, listener: OnItemClickListener) {
            cityNameText.text = city
            itemView.setOnClickListener {
                listener.onItemClick(city)
            }
        }

        fun bindTemperature(temp: Double) {
            tempText.text = "${temp}°C"
        }
    }

    private fun fetchWeatherData(city: String, callback: (Double) -> Unit) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build()
            .create(ApiInterface::class.java)

        val call = retrofit.getWeatherData(city, "f648b4b6f9e9b11383ec6b3187cc253e", "metric")
        call.enqueue(object : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    callback(responseBody.main.temp)
                } else {
                    // Handle error case
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                // Handle failure case
            }
        })
    }
}
