package cl.planb.logic

import cl.planb.model.*
import java.time.LocalDate

object Reglas {
    fun lugar(l: Lugar): Map<String, String> {
        val e = mutableMapOf<String, String>()
        if (l.nombre.trim().length !in 3..80) e["nombre"] = "Escribe entre 3 y 80 caracteres"
        if (l.descripcion.trim().length !in 3..300)
            e["descripcion"] = "Escribe entre 3 y 300 caracteres"
        if (l.costo !in 0..1000000) e["costo"] = "Ingresa un costo entre 0 y 1.000.000"
        if (l.minutos !in 5..1440) e["minutos"] = "Ingresa entre 5 y 1440 minutos"
        if (!l.latitud.isFinite() || l.latitud !in -90.0..90.0)
            e["latitud"] = "Latitud entre -90 y 90"
        if (!l.longitud.isFinite() || l.longitud !in -180.0..180.0)
            e["longitud"] = "Longitud entre -180 y 180"
        return e
    }

    fun plan(p: Plan): Map<String, String> {
        val e = mutableMapOf<String, String>()
        if (p.titulo.trim().length !in 3..80) e["titulo"] = "Escribe entre 3 y 80 caracteres"
        if (p.lugarNombre.trim().length !in 3..80)
            e["lugarNombre"] = "Escribe el lugar (3 a 80 caracteres)"
        try {
            LocalDate.parse(p.fecha)
        } catch (error: Exception) {
            e["fecha"] = "Usa una fecha válida: AAAA-MM-DD"
        }
        if (p.estado !in listOf("PENDIENTE", "REALIZADO", "CANCELADO"))
            e["estado"] = "Estado inválido"
        if (p.notas.length > 300) e["notas"] = "Máximo 300 caracteres"
        if (p.valoracion !in 0..5 || (p.estado != "REALIZADO" && p.valoracion != 0))
            e["valoracion"] = "Valora de 0 a 5 solo un plan realizado"
        return e
    }

    fun sugerir(
        lugares: List<Lugar>,
        minutos: Int,
        presupuesto: Int,
        lluvia: Boolean,
    ): List<Lugar> =
        lugares
            .filter { it.minutos <= minutos && it.costo <= presupuesto && (!lluvia || it.interior) }
            .sortedWith(compareBy<Lugar> { it.costo }.thenBy { it.minutos })

    fun clima(c: CurrentWeather): Clima {
        val lluvia = c.precipitation > 0 || c.weather_code in 51..99
        return Clima(
            c.temperature_2m,
            lluvia,
            if (lluvia) "Conviene un plan bajo techo"
            else "Puedes considerar un plan al aire libre",
        )
    }
}
