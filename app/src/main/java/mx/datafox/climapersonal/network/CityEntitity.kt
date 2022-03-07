package mx.datafox.climapersonal.network

import mx.datafox.climapersonal.model.City
import mx.datafox.climapersonal.model.Current
import java.io.Serializable

data class CityEntitity(
    val current: Current,
    val city: City?
): Serializable