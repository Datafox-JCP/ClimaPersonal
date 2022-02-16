package mx.datafox.climapersonal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import coil.load
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.datafox.climapersonal.databinding.ActivityMainBinding
import mx.datafox.climapersonal.databinding.ModeloBinding
import mx.datafox.climapersonal.network.WeatherEntity
import mx.datafox.climapersonal.network.WeatherService
import mx.datafox.climapersonal.utils.checkForInternet
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ModeloBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ModeloBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewData()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actions_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_actualizar -> {
                Toast.makeText(this, "Menú seleccionado", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupViewData() {

        if (checkForInternet(this)) {
            lifecycleScope.launch {
                formatResponse(getWeather())
            }
        } else {
            showError("Sin acceso a Internet")
            binding.detailsContainer.isVisible = false
        }
    }

    private fun formatResponse(weatherEntity: WeatherEntity){
        try {
            val temp = "${weatherEntity.main.temp.toInt()}º"
            val cityName = weatherEntity.name
            val country = weatherEntity.sys.country
            val address = "$cityName, $country"
            val tempMin = "Mín: ${weatherEntity.main.temp_min.toInt()}º"
            val tempMax = "Max: ${weatherEntity.main.temp_max.toInt()}º"
            val status = weatherEntity.weather[0].description.uppercase()
            val dt = weatherEntity.dt
            val updatedAt = "Actualizado: ${SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(dt*1000))}"
            val sunrise = weatherEntity.sys.sunrise
            val sunriseFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise*1000))
            val sunset = weatherEntity.sys.sunset
            val sunsetFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset*1000))
            val wind = "${weatherEntity.wind.speed} km/h"
            val pressure = "${weatherEntity.main.pressure} mb"
            val humidity = "${weatherEntity.main.humidity}%"
            val feelsLike = "Sensación: ${weatherEntity.main.feels_like.toInt()}º"
            val icon = weatherEntity.weather[0].icon
            val iconUrl = "https://openweathermap.org/img/w/$icon.png"

            binding.apply {
                iconImageView.load(iconUrl)
                adressTextView.text = address
                dateTextView.text = updatedAt
                temperatureTextView.text = temp
                statusTextView.text = status
                tempMinTextView.text = tempMin
                tempMaxTextView.text = tempMax
                sunriseTextView.text = sunriseFormat
                sunsetTextView.text = sunsetFormat
                windTextView.text = wind
                pressureTextView.text = pressure
                humidityTextView.text = humidity
                detailsContainer.isVisible = true
                feelsLikeTextView.text = feelsLike
            }

            showIndicator(false)
        } catch (exception: Exception) {
            showError("Ha ocurrido un error con los datos")
            Log.e("Error format", "Ha ocurrido un error")
            showIndicator(false)
        }
    }

    private suspend fun getWeather(): WeatherEntity = withContext(Dispatchers.IO){
        showIndicator(true)

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service: WeatherService = retrofit.create(WeatherService::class.java)

        service.getWeatherById(3516035L, "metric", "sp", "")
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showIndicator(visible: Boolean) {
        binding.progressBarIndicator.isVisible = visible
    }


}