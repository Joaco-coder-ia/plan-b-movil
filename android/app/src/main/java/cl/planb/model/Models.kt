package cl.planb.model

data class Lugar(
    val id: Long? = null,
    val nombre: String = "",
    val descripcion: String = "",
    val costo: Int = 0,
    val minutos: Int = 30,
    val interior: Boolean = true,
    val latitud: Double = -33.45,
    val longitud: Double = -70.66,
)

data class Plan(
    val id: Long? = null,
    val titulo: String = "",
    val lugarNombre: String = "",
    val fecha: String = "",
    val estado: String = "PENDIENTE",
    val notas: String = "",
    val valoracion: Int = 0,
)

data class Clima(val temperatura: Double, val lluvia: Boolean, val descripcion: String)

data class WeatherResponse(val current: CurrentWeather)

data class CurrentWeather(
    val temperature_2m: Double,
    val precipitation: Double,
    val weather_code: Int,
)

data class UiState(
    val lugares: List<Lugar> = emptyList(),
    val planes: List<Plan> = emptyList(),
    val clima: Clima? = null,
    val cargando: Boolean = false,
    val mensaje: String? = null,
    val errorClima: String? = null,
    val errores: Map<String, String> = emptyMap(),
)
