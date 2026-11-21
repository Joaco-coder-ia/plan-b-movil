package cl.planb.data

import cl.planb.model.*
import retrofit2.http.*

interface LugaresApi {
    @GET("api/lugares") suspend fun listar(): List<Lugar>

    @POST("api/lugares") suspend fun crear(@Body lugar: Lugar): Lugar

    @PUT("api/lugares/{id}") suspend fun editar(@Path("id") id: Long, @Body lugar: Lugar): Lugar

    @DELETE("api/lugares/{id}") suspend fun eliminar(@Path("id") id: Long)
}

interface PlanesApi {
    @GET("api/planes") suspend fun listar(): List<Plan>

    @POST("api/planes") suspend fun crear(@Body plan: Plan): Plan

    @PUT("api/planes/{id}") suspend fun editar(@Path("id") id: Long, @Body plan: Plan): Plan

    @DELETE("api/planes/{id}") suspend fun eliminar(@Path("id") id: Long)
}

interface WeatherApi {
    @GET("v1/forecast")
    suspend fun consultar(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        @Query("current") current: String = "temperature_2m,precipitation,weather_code",
    ): WeatherResponse
}

interface Repository {
    suspend fun lugares(): List<Lugar>

    suspend fun planes(): List<Plan>

    suspend fun guardar(lugar: Lugar)

    suspend fun guardar(plan: Plan)

    suspend fun borrarLugar(id: Long)

    suspend fun borrarPlan(id: Long)

    suspend fun clima(lat: Double, lon: Double): Clima
}
