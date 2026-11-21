package cl.planb

import cl.planb.logic.Reglas
import cl.planb.model.*
import org.junit.Assert.*
import org.junit.Test

class ReglasTest {
    private val lugar =
        Lugar(nombre = "Biblioteca", descripcion = "Leer un libro", costo = 0, minutos = 30)
    private val plan =
        Plan(titulo = "Leer un libro", lugarNombre = "Biblioteca", fecha = "2026-12-01")

    @Test
    fun lugarValido() {
        assertTrue(Reglas.lugar(lugar).isEmpty())
    }

    @Test
    fun erroresSeparados() {
        assertEquals(
            setOf("nombre", "descripcion", "costo", "minutos", "latitud", "longitud"),
            Reglas.lugar(Lugar(costo = -1, minutos = 0, latitud = 91.0, longitud = -181.0)).keys,
        )
    }

    @Test
    fun limites() {
        assertTrue(
            Reglas.lugar(
                    lugar.copy(costo = 1000000, minutos = 1440, latitud = -90.0, longitud = 180.0),
                )
                .isEmpty(),
        )
        assertEquals(
            6,
            Reglas.lugar(
                    lugar.copy(
                        nombre = "a".repeat(81),
                        descripcion = "x".repeat(301),
                        costo = 1000001,
                        minutos = 1441,
                        latitud = Double.NaN,
                        longitud = Double.POSITIVE_INFINITY,
                    ),
                )
                .size,
        )
    }

    @Test
    fun planValido() {
        assertTrue(Reglas.plan(plan).isEmpty())
    }

    @Test
    fun validaFechaReal() {
        assertTrue(Reglas.plan(plan.copy(fecha = "2026-02-30")).containsKey("fecha"))
    }

    @Test
    fun erroresDelPlan() {
        assertEquals(
            6,
            Reglas.plan(
                    Plan(fecha = "no", estado = "OTRO", notas = "x".repeat(301), valoracion = 8),
                )
                .size,
        )
    }

    @Test
    fun valoracionSoloRealizados() {
        assertTrue(Reglas.plan(plan.copy(valoracion = 2)).containsKey("valoracion"))
        assertTrue(Reglas.plan(plan.copy(estado = "REALIZADO", valoracion = 5)).isEmpty())
        assertTrue(Reglas.plan(plan.copy(estado = "CANCELADO")).isEmpty())
    }

    @Test
    fun filtraYOrdena() {
        val exterior = lugar.copy(id = 2, costo = 1000, interior = false)
        val caro = lugar.copy(id = 3, costo = 5001)
        val largo = lugar.copy(id = 4, minutos = 61)
        assertEquals(
            listOf(lugar),
            Reglas.sugerir(listOf(exterior, caro, largo, lugar), 60, 5000, true),
        )
        assertEquals(
            listOf(lugar, exterior),
            Reglas.sugerir(listOf(exterior, lugar), 60, 5000, false),
        )
        assertTrue(Reglas.sugerir(listOf(lugar), 0, 0, false).isEmpty())
    }

    @Test
    fun igualdadDeLimitesIncluyeLugar() {
        assertEquals(1, Reglas.sugerir(listOf(lugar), 30, 0, false).size)
    }

    @Test
    fun climaPorCodigoOPrecipitacion() {
        assertFalse(Reglas.clima(CurrentWeather(20.0, 0.0, 0)).lluvia)
        assertTrue(Reglas.clima(CurrentWeather(10.0, 0.3, 0)).lluvia)
        assertTrue(Reglas.clima(CurrentWeather(10.0, 0.0, 61)).lluvia)
    }
}
