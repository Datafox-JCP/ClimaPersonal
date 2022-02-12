package mx.datafox.climapersonal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.datafox.climapersonal.databinding.ActivityMainBinding
import mx.datafox.climapersonal.network.WeatherEntity
import mx.datafox.climapersonal.network.WeatherService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()
    }

    private fun setupActionBar() {
        lifecycleScope.launch {
            formatResponse(getWeather())
        }
    }

    private suspend fun getWeather(): WeatherEntity = withContext(Dispatchers.IO){
        setupTitle("Consultando")

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service: WeatherService = retrofit.create(WeatherService::class.java)

        service.getWeatherById(4005539L, "metric", "codigo de OW aquí")
    }

    private fun setupTitle(newTitle: String) {
        supportActionBar?.let { title = newTitle }
    }

    private fun formatResponse(weatherEntity: WeatherEntity){
        val temp = "${weatherEntity.main.temp} Cº"
        val name = weatherEntity.name
        val country = weatherEntity.sys.country
        setupTitle("$temp en $name, $country")
    }
}
