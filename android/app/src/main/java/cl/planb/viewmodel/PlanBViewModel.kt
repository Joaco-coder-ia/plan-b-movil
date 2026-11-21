package cl.planb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.planb.data.Repository
import cl.planb.logic.Reglas
import cl.planb.model.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlanBViewModel(private val repository: Repository) : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    private fun operar(accion: suspend () -> Unit) {
        if (_state.value.cargando) return
        _state.value = _state.value.copy(cargando = true, mensaje = null)
        viewModelScope.launch {
            try {
                accion()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.value =
                    _state.value.copy(
                        mensaje =
                            "No se pudo completar. Revisa la conexión con los servicios y vuelve a intentar.",
                    )
            } finally {
                _state.value = _state.value.copy(cargando = false)
            }
        }
    }

    private suspend fun cargarDatos() {
        val lugares = repository.lugares()
        val planes = repository.planes()
        _state.value = _state.value.copy(lugares = lugares, planes = planes)
    }

    fun cargar() = operar { cargarDatos() }

    fun limpiarErrores() {
        _state.value = _state.value.copy(errores = emptyMap(), mensaje = null)
    }

    fun guardar(lugar: Lugar, onSuccess: () -> Unit = {}) {
        val errores = Reglas.lugar(lugar)
        _state.value = _state.value.copy(errores = errores)
        if (errores.isNotEmpty()) return
        operar {
            repository.guardar(lugar)
            _state.value = _state.value.copy(mensaje = "Lugar guardado")
            onSuccess()
            cargarDatos()
        }
    }

    fun guardar(plan: Plan, onSuccess: () -> Unit = {}) {
        val errores = Reglas.plan(plan)
        _state.value = _state.value.copy(errores = errores)
        if (errores.isNotEmpty()) return
        operar {
            repository.guardar(plan)
            _state.value = _state.value.copy(mensaje = "Plan guardado")
            onSuccess()
            cargarDatos()
        }
    }

    fun borrarLugar(id: Long) = operar {
        repository.borrarLugar(id)
        cargarDatos()
        _state.value =
            _state.value.copy(
                mensaje = "Lugar eliminado; tus planes anteriores conservan su nombre",
            )
    }

    fun borrarPlan(id: Long) = operar {
        repository.borrarPlan(id)
        cargarDatos()
        _state.value = _state.value.copy(mensaje = "Plan eliminado")
    }

    fun consultarClima(lat: Double, lon: Double) {
        viewModelScope.launch {
            _state.value = _state.value.copy(clima = null, errorClima = null)
            try {
                _state.value = _state.value.copy(clima = repository.clima(lat, lon))
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.value =
                    _state.value.copy(
                        errorClima =
                            "Clima no disponible. Se mostrarán solo opciones bajo techo hasta reintentar.",
                    )
            }
        }
    }
}
