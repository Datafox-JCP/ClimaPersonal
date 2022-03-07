package mx.datafox.climapersonal.network

import mx.datafox.climapersonal.model.City
import retrofit2.http.GET
import retrofit2.http.Query

interface CityServices {
    @GET("geo/1.0/reverse")
    suspend fun getCitiesByLatLong(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appId") appId: String
    ): List<City>
}