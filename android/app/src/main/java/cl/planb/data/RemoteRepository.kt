package cl.planb.data

import cl.planb.logic.Reglas
import cl.planb.model.*

class RemoteRepository(
    private val lugaresApi: LugaresApi,
    private val planesApi: PlanesApi,
    private val weatherApi: WeatherApi,
) : Repository {
    override suspend fun lugares() = lugaresApi.listar()

    override suspend fun planes() = planesApi.listar()

    override suspend fun guardar(lugar: Lugar) {
        if (lugar.id == null) lugaresApi.crear(lugar) else lugaresApi.editar(lugar.id, lugar)
    }

    override suspend fun guardar(plan: Plan) {
        if (plan.id == null) planesApi.crear(plan) else planesApi.editar(plan.id, plan)
    }

    override suspend fun borrarLugar(id: Long) = lugaresApi.eliminar(id)

    override suspend fun borrarPlan(id: Long) = planesApi.eliminar(id)

    override suspend fun clima(lat: Double, lon: Double) =
        Reglas.clima(weatherApi.consultar(lat, lon).current)
}
