package mx.datafox.climapersonal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.datafox.climapersonal.databinding.ActivityMainBinding
import mx.datafox.climapersonal.network.WeatherEntity
import mx.datafox.climapersonal.network.WeatherService
import mx.datafox.climapersonal.utils.checkForInternet
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewData()
    }

    private fun setupViewData() {

        if (checkForInternet(this)) {
            lifecycleScope.launch {
                formatResponse(getWeather())
            }
        } else {
            showError("Sin acceso a Internet")
        }
    }

    private fun formatResponse(weatherEntity: WeatherEntity){
        try {
            val temp = "${weatherEntity.main.temp.toInt()}º"
            val cityName = weatherEntity.name
            val country = weatherEntity.sys.country
            val address = "$cityName, $country"
            val dateNow = Calendar.getInstance().time
            val tempMin = "Min: ${weatherEntity.main.temp_min.toInt()}º"
            val tempMax = "Max: ${weatherEntity.main.temp_max.toInt()}º"
            val status = "Sensación: ${weatherEntity.main.feels_like.toInt()}º"

            binding.apply {
                adressTextView.text = address
                dateTextView.text = dateNow.toString()
                temperatureTextView.text = temp
                statusTextView.text = status
                tempMinTextView.text = tempMin
                tempMaxTextView.text = tempMax
            }

            showIndicator(false)
        } catch (exception: Exception) {
            showError("Ha ocurrido un error")
            Log.e("Error format", "Ha ocurrido un error")
        }
    }

    private suspend fun getWeather(): WeatherEntity = withContext(Dispatchers.IO){
        showIndicator(true)

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service: WeatherService = retrofit.create(WeatherService::class.java)

        service.getWeatherById(3516035L, "metric", "30ba6cd1ad33ea67e2dfd78a8d28ae62")
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showIndicator(visible: Boolean) {
        binding.progressBarIndicator.isVisible = visible
    }


}