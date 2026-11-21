package cl.planb

import cl.planb.data.Repository
import cl.planb.model.*
import cl.planb.viewmodel.PlanBViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeRepository
    private lateinit var vm: PlanBViewModel
    private val lugar = Lugar(id = 1, nombre = "Biblioteca", descripcion = "Leer un libro")
    private val plan =
        Plan(id = 1, titulo = "Leer", lugarNombre = "Biblioteca", fecha = "2026-12-01")

    @Before
    fun preparar() {
        Dispatchers.setMain(dispatcher)
        repo = FakeRepository()
        vm = PlanBViewModel(repo)
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    @Test
    fun cargaDatos() =
        runTest(dispatcher) {
            repo.lugares = listOf(lugar)
            repo.planes = listOf(plan)
            vm.cargar()
            assertTrue(vm.state.value.cargando)
            advanceUntilIdle()
            assertEquals(listOf(lugar), vm.state.value.lugares)
            assertEquals(listOf(plan), vm.state.value.planes)
            assertFalse(vm.state.value.cargando)
        }

    @Test
    fun noDuplicaOperacionEnCurso() =
        runTest(dispatcher) {
            vm.cargar()
            vm.cargar()
            advanceUntilIdle()
            assertEquals(1, repo.consultas)
        }

    @Test
    fun guardaLugarYPlan() =
        runTest(dispatcher) {
            var callbacks = 0
            vm.guardar(lugar) { callbacks++ }
            advanceUntilIdle()
            assertEquals("Lugar guardado", vm.state.value.mensaje)
            vm.guardar(plan) { callbacks++ }
            advanceUntilIdle()
            assertEquals("Plan guardado", vm.state.value.mensaje)
            assertEquals(2, callbacks)
            assertEquals(listOf(lugar), vm.state.value.lugares)
            assertEquals(listOf(plan), vm.state.value.planes)
        }

    @Test
    fun noGuardaDatosInvalidos() =
        runTest(dispatcher) {
            vm.guardar(Lugar())
            advanceUntilIdle()
            assertTrue(vm.state.value.errores.containsKey("nombre"))
            assertTrue(repo.lugares.isEmpty())
            vm.guardar(Plan())
            advanceUntilIdle()
            assertTrue(vm.state.value.errores.containsKey("titulo"))
            assertTrue(repo.planes.isEmpty())
            vm.limpiarErrores()
            assertTrue(vm.state.value.errores.isEmpty())
            assertNull(vm.state.value.mensaje)
        }

    @Test
    fun eliminaAmbosTipos() =
        runTest(dispatcher) {
            repo.lugares = listOf(lugar)
            repo.planes = listOf(plan)
            vm.borrarLugar(1)
            advanceUntilIdle()
            assertTrue(vm.state.value.lugares.isEmpty())
            assertNotNull(vm.state.value.mensaje)
            vm.borrarPlan(1)
            advanceUntilIdle()
            assertTrue(vm.state.value.planes.isEmpty())
            assertEquals("Plan eliminado", vm.state.value.mensaje)
        }

    @Test
    fun errorNoBorraUltimaLista() =
        runTest(dispatcher) {
            repo.lugares = listOf(lugar)
            vm.cargar()
            advanceUntilIdle()
            repo.falla = true
            vm.cargar()
            advanceUntilIdle()
            assertEquals(listOf(lugar), vm.state.value.lugares)
            assertFalse(vm.state.value.cargando)
            assertNotNull(vm.state.value.mensaje)
        }

    @Test
    fun consultaClimaYControlaError() =
        runTest(dispatcher) {
            vm.consultarClima(-33.45, -70.66)
            advanceUntilIdle()
            assertEquals(20.0, vm.state.value.clima!!.temperatura, 0.01)
            repo.falla = true
            vm.consultarClima(0.0, 0.0)
            advanceUntilIdle()
            assertNull(vm.state.value.clima)
            assertNotNull(vm.state.value.errorClima)
        }

    @Test
    fun erroresDeEscrituraNoInvocanExito() =
        runTest(dispatcher) {
            repo.falla = true
            var exito = false
            vm.guardar(lugar) { exito = true }
            advanceUntilIdle()
            assertFalse(exito)
            assertNotNull(vm.state.value.mensaje)
            vm.guardar(plan) { exito = true }
            advanceUntilIdle()
            assertFalse(exito)
            vm.borrarLugar(1)
            advanceUntilIdle()
            assertFalse(vm.state.value.cargando)
            vm.borrarPlan(1)
            advanceUntilIdle()
            assertFalse(vm.state.value.cargando)
        }

    class FakeRepository : Repository {
        var lugares = listOf<Lugar>()
        var planes = listOf<Plan>()
        var falla = false
        var consultas = 0

        private fun comprobar() {
            if (falla) throw java.io.IOException("Sin red")
        }

        override suspend fun lugares(): List<Lugar> {
            comprobar()
            consultas++
            return lugares
        }

        override suspend fun planes(): List<Plan> {
            comprobar()
            return planes
        }

        override suspend fun guardar(lugar: Lugar) {
            comprobar()
            lugares = listOf(lugar)
        }

        override suspend fun guardar(plan: Plan) {
            comprobar()
            planes = listOf(plan)
        }

        override suspend fun borrarLugar(id: Long) {
            comprobar()
            lugares = lugares.filterNot { it.id == id }
        }

        override suspend fun borrarPlan(id: Long) {
            comprobar()
            planes = planes.filterNot { it.id == id }
        }

        override suspend fun clima(lat: Double, lon: Double): Clima {
            comprobar()
            return Clima(20.0, false, "Despejado")
        }
    }
}
