package cl.planb

import cl.planb.data.*
import cl.planb.model.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import org.junit.Assert.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repo: RemoteRepository

    @Before
    fun preparar() {
        server = MockWebServer()
        server.start()
        val retrofit =
            Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        repo =
            RemoteRepository(
                retrofit.create(LugaresApi::class.java),
                retrofit.create(PlanesApi::class.java),
                retrofit.create(WeatherApi::class.java),
            )
    }

    @After
    fun cerrar() {
        server.shutdown()
    }

    @Test
    fun contratosCrudLugares() = runBlocking {
        server.enqueue(MockResponse().setBody("[]"))
        assertTrue(repo.lugares().isEmpty())
        assertEquals("/api/lugares", server.takeRequest().path)
        server.enqueue(MockResponse().setBody("{\"id\":1}"))
        repo.guardar(Lugar(nombre = "Biblioteca"))
        assertEquals("POST", server.takeRequest().method)
        server.enqueue(MockResponse().setBody("{\"id\":1}"))
        repo.guardar(Lugar(id = 1))
        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/lugares/1", request.path)
        server.enqueue(MockResponse().setResponseCode(204))
        repo.borrarLugar(1)
        assertEquals("DELETE", server.takeRequest().method)
    }

    @Test
    fun contratosCrudPlanes() = runBlocking {
        server.enqueue(MockResponse().setBody("[]"))
        assertTrue(repo.planes().isEmpty())
        assertEquals("/api/planes", server.takeRequest().path)
        server.enqueue(MockResponse().setBody("{\"id\":2}"))
        repo.guardar(Plan(titulo = "Pausa"))
        assertEquals("POST", server.takeRequest().method)
        server.enqueue(MockResponse().setBody("{\"id\":2}"))
        repo.guardar(Plan(id = 2))
        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/planes/2", request.path)
        server.enqueue(MockResponse().setResponseCode(204))
        repo.borrarPlan(2)
        assertEquals("DELETE", server.takeRequest().method)
    }

    @Test
    fun climaSeDeserializaYUsaCoordenadas() = runBlocking {
        server.enqueue(
            MockResponse()
                .setBody(
                    "{\"current\":{\"temperature_2m\":18.5,\"precipitation\":0,\"weather_code\":0}}",
                ),
        )
        val clima = repo.clima(-33.45, -70.66)
        assertEquals(18.5, clima.temperatura, 0.01)
        assertFalse(clima.lluvia)
        val path = server.takeRequest().path!!
        assertTrue(path.contains("latitude=-33.45"))
        assertTrue(path.contains("longitude=-70.66"))
    }

    @Test
    fun falloHttpSePropaga() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500))
        try {
            repo.lugares()
            fail("Debía propagar el error")
        } catch (error: retrofit2.HttpException) {
            assertEquals(500, error.code())
        }
    }
}
