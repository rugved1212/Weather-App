package com.example.wheaterapp

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.weatherapp.CityAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalTime


class MainActivity : AppCompatActivity(), CityAdapter.OnItemClickListener {

    var cityName: String = ""
    var countryName: String = ""

    var shared_city: String = ""

    private lateinit var cityManager: CityManager
    private lateinit var cityAdapter: CityAdapter
    lateinit var city_text : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        cityManager = CityManager(this)

        val sharedPref = getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        shared_city = sharedPref.getString("my_city", "").toString()

        cityName = intent.getStringExtra("city").toString()
        countryName = intent.getStringExtra("country").toString()

        city_text = findViewById(R.id.loc_text)

        if (cityName.isNotEmpty()) {
            fetchWeatherData(cityName)
        } else if (shared_city.isNotEmpty()) {
            fetchWeatherData(shared_city)
        } else {
            fetchWeatherData("Mumbai")
        }

        val search_View = findViewById<SearchView>(R.id.search_view)
        val textViewId = search_View.context.resources.getIdentifier("android:id/search_src_text", null, null)
        val searchText = search_View.findViewById<TextView>(textViewId)
        searchText?.setTextColor(Color.WHITE)

        search_View.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(p0: String?): Boolean {
                    if (!p0.isNullOrEmpty()) {
                        cityName = p0
                        fetchWeatherData(cityName)
                    }
                    search_View.setQuery("", false)
                    search_View.clearFocus()
                    search_View.isIconified = true
                    return true
                }

                override fun onQueryTextChange(p0: String?): Boolean {
                    return false
                }

            }
        )

        cityAdapter = CityAdapter(cityManager.getCities().toList(), this)
        val recyclerView = findViewById<RecyclerView>(R.id.list_of_cities)
        recyclerView.layoutManager = LinearLayoutManager(this,  LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = cityAdapter
    }

    private fun fetchWeatherData(city: String) {

        val loadingIndicator = findViewById<ProgressBar>(R.id.loading)
        loadingIndicator.visibility = View.VISIBLE

        val temp_text: TextView = findViewById(R.id.temp_text)
        val temp_min_max : TextView = findViewById(R.id.temp_min_max)
        val weather_text : TextView = findViewById(R.id.wheather_name)
        val humdity_text : TextView = findViewById(R.id.humidity_text)
        val wind_text : TextView = findViewById(R.id.wind_text)
        val weather_bg : RelativeLayout = findViewById(R.id.main)
        val weather_img : LottieAnimationView = findViewById(R.id.wheather_img)

        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build()
            .create(ApiInterface::class.java)

        val response = retrofit.getWeatherData(city, "f648b4b6f9e9b11383ec6b3187cc253e", "metric")
        response.enqueue(object : Callback<WeatherApp>{
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()
                loadingIndicator.visibility = View.GONE

                if (response.isSuccessful && responseBody != null) {
                    val temperature = responseBody.main.temp
                    val min = responseBody.main.temp_min
                    val city_api = responseBody.name
                    val country_api = responseBody.sys.country
                    val max = responseBody.main.temp_max
                    val hum = responseBody.main.humidity
                    val wind_s = responseBody.wind.speed
                    val weather = responseBody.weather.firstOrNull()?.main?: "unknown"

                    cityManager.addCity(city_api)
                    city_text.text = city_api + ", " + country_api
                    updateRecyclerView()

                    weather_text.text = weather
                    temp_text.text = "$temperature°C"
                    temp_min_max.text = "Min: $min°C  ---  Max: $max°C"
                    humdity_text.text = "$hum"
                    wind_text.text = "$wind_s"+"m/s"

                    val currenttime = LocalTime.now()
                    val startofday = LocalTime.of(6,0)
                    val endofday = LocalTime.of(18,0)

                    if (weather != null) {
                        if (currenttime.isAfter(startofday) && currenttime.isBefore(endofday)) {
                            weather_bg.setBackgroundResource(R.drawable.day_gradient)
                            when(weather) {
                                "Sunny", "Clear" -> {
                                    weather_img.setAnimation(R.raw.sunny)
                                }
                                "Haze", "Clouds", "Mist" -> {
                                    weather_img.setAnimation(R.raw.haze)
                                }
                                "Snow" -> {
                                    weather_img.setAnimation(R.raw.daysnow)
                                }
                                "Rain", "Drizzle" -> {
                                    weather_img.setAnimation(R.raw.dayrain)
                                }
                                "Thunderstorm" -> {
                                    weather_img.setAnimation(R.raw.daylightning)
                                }
                            }
                        }
                        else {
                            weather_bg.setBackgroundResource(R.drawable.night_gradient)
                            when(weather) {
                                "Night", "Clear" -> {
                                    weather_img.setAnimation(R.raw.moon)
                                }
                                "Haze", "Clouds", "Mist" -> {
                                    weather_img.setAnimation(R.raw.haze)
                                }
                                "Snow" -> {
                                    weather_img.setAnimation(R.raw.nightsnow)
                                }
                                "Rain", "Drizzle" -> {
                                    weather_img.setAnimation(R.raw.nightrain)
                                }
                                "Thunderstorm" -> {
                                    weather_img.setAnimation(R.raw.nightlighthing)
                                }
                            }
                        }
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Unable to find the city. Please enter the correct name!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                loadingIndicator.visibility = View.GONE
            }

        })

    }

    private fun updateRecyclerView() {
        val cities = cityManager.getCities().toList()
        cityAdapter.updateCities(cities)
    }

    override fun onItemClick(city: String) {
        fetchWeatherData(city)
    }
}
