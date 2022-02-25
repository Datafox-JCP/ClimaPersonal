package mx.datafox.climapersonal.network

import mx.datafox.climapersonal.model.Main
import mx.datafox.climapersonal.model.Sys
import mx.datafox.climapersonal.model.Weather
import mx.datafox.climapersonal.model.Wind

data class WeatherEntity(
    val base: String,
    val main: Main,
    val sys: Sys,
    val id: Int,
    val name: String,
    val wind: Wind,
    val weather: List<Weather>,
    val dt: Long
)