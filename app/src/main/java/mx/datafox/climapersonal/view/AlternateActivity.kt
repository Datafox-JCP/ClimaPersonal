package mx.datafox.climapersonal.view

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import mx.datafox.climapersonal.R
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class AlternateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        weatherTask().execute()
    }

    inner class weatherTask(): AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            // Muestra el ProgressBar
            findViewById<ProgressBar>(R.id.progressBarIndicator).visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: String?): String {
            val city = "Temixco"
            val api = ""

            val response: String = try {
                URL("https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&appid=$api").readText(
                    Charsets.UTF_8
                )
            } catch(e: Exception) {
                e.toString()
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

                val lastUpdate: Long = jsonObj.getLong("dt")
                val updatedAt = "Actualizado el:" + SimpleDateFormat("dd/mm/yyyyhh:mma", Locale.ENGLISH).format(lastUpdate*1000)
                val temp = main.getString("temp") + "Cº"
                val tempMin = "Temp mín: " + main.getString("temp_min") + "Cº"
                val tempMax = "Temp max: " + main.getString("temp_max") + "Cº"
                val pressure = main.getString("pressure")
                val humidity = main.getString("humidity")
                val sunrise: Long = sys.getLong("sunrise")
                val sunset: Long = sys.getLong("sunset")
                val windSpeed = wind.getString("speed")
                val weatherDescription = weather.getString("description")

                findViewById<TextView>(R.id.dateTextView).text = updatedAt
                findViewById<TextView>(R.id.statusTextView).text = weatherDescription.capitalize()
                findViewById<TextView>(R.id.temperatureTextView).text = temp
                findViewById<TextView>(R.id.tempMaxTextView).text = tempMax
                findViewById<TextView>(R.id.tempMinTextView).text = tempMin
                findViewById<TextView>(R.id.sunriseTextView).text = SimpleDateFormat("hh:mma", Locale.ENGLISH).format(Date(sunrise*1000))
                findViewById<TextView>(R.id.sunsetTextView).text = SimpleDateFormat("hh:mma", Locale.ENGLISH).format(Date(sunset*1000))
                findViewById<TextView>(R.id.pressureTextView).text = pressure
                findViewById<TextView>(R.id.humidityTextView).text = humidity
                findViewById<TextView>(R.id.windTextView).text = windSpeed

                findViewById<ProgressBar>(R.id.progressBarIndicator).visibility = View.GONE
                findViewById<View>(R.id.detailsContainer).visibility = View.VISIBLE

            } catch (e: Exception) {
                Log.e("Error", e.localizedMessage)
                findViewById<ProgressBar>(R.id.progressBarIndicator).visibility = View.GONE
                findViewById<View>(R.id.detailsContainer).visibility = View.GONE
            }
        }
    }
}