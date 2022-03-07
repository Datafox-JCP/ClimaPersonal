package mx.datafox.climapersonal.network

import retrofit2.http.GET
import retrofit2.http.Query


interface WeatherService {
    //@GET("data/2.5/weather")
    // api.openweathermap.org/data/2.5/weather?lat=35&lon=139&appid={API key}
    @GET("data/2.5/onecall")
    // api.openweathermap.org/data/2.5/onecall?lat={lat}&lon={lon}&exclude={part}&appid={API key}
    suspend fun getWeatherById(
        //@Query("id") lon: Long,
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("units") units: String?,
        @Query("lang") lang: String?,  // Para el idioma
        @Query("appid") appid: String): WeatherEntity
}