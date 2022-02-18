package mx.datafox.climapersonal

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.datafox.climapersonal.BuildConfig.APPLICATION_ID
import mx.datafox.climapersonal.databinding.ModeloBinding
import mx.datafox.climapersonal.network.WeatherEntity
import mx.datafox.climapersonal.network.WeatherService
import mx.datafox.climapersonal.utils.checkForInternet
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivityError"
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34

    private var latitude = ""
    private var longitude = ""

    private lateinit var binding: ModeloBinding

    /**
     * Punto de entrada para el API Fused Location Provider.
     */
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ModeloBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupViewData()
    }

    /**
     * Funciones de menú
     */

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

    /**
     * Función para consultar y cargar datos de clima
     */

    private fun setupViewData() {

        if (checkForInternet(this)) {
            lifecycleScope.launch {
                getLastLocation()
                formatResponse(getWeather())
            }
        } else {
            showError("Sin acceso a Internet")
            binding.detailsContainer.isVisible = false
        }
    }

    private suspend fun getWeather(): WeatherEntity = withContext(Dispatchers.IO){
        Log.e(TAG, "CORR Lat: $latitude Long: $longitude")
        showIndicator(true)

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service: WeatherService = retrofit.create(WeatherService::class.java)

        service.getWeatherById(latitude, longitude, "metric", "sp", "30ba6cd1ad33ea67e2dfd78a8d28ae62")
    }

    /**
     * Función para mostrar los datos obtenidos de OpenWeather
     */

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

    /**
     * Complementarios para errores y visibilidad de las views
     */

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showIndicator(visible: Boolean) {
        binding.progressBarIndicator.isVisible = visible
    }

    /**
     * Provee un forma sencilla de obtener la ubicación del dispositivo, muy adecuada para
     * applicaciones que no requieren de una alta preción de la uniación y que no requieran
     * actualizaciones. Obtiene lo mejor y y más reciente ubicación disponible, que en algunos
     * casos puede llegar a ser nula, cuando la ubicación no este disponible.
     *
     * Nota: Este método debe llamarse después que los permispos de ubicación fueron otorgados.
     */

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation
            .addOnCompleteListener { taskLocation ->
                if (taskLocation.isSuccessful && taskLocation.result != null) {

                    val location = taskLocation.result

                    latitude = location?.latitude.toString()
                    longitude = location?.longitude.toString()
                    Log.d(TAG, "GetLasLoc Lat: $latitude Long: $longitude")
                } else {
                    Log.w(TAG, "getLastLocation:exception", taskLocation.exception)
                    showSnackbar(R.string.no_location_detected)
                }
            }
    }

    /**
     * Devuelve el estado de los permisos que se necesitan
     */

    private fun checkPermissions() =
        ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(this, arrayOf(ACCESS_COARSE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE)
    }
    /**
     * Muestra el [Snackbar].
     *
     * @param snackStrId El id del recurso para el el texto en el Snackbar.
     * @param actionStrId El texto para el elemento de acciín.
     * @param listener El listener asociado con la acción del Snackbar.
     */
    private fun showSnackbar(
        snackStrId: Int,
        actionStrId: Int = 0,
        listener: View.OnClickListener? = null
    ) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), getString(snackStrId),
            LENGTH_INDEFINITE)
        if (actionStrId != 0 && listener != null) {
            snackbar.setAction(getString(actionStrId), listener)
        }
        snackbar.show()
    }

}