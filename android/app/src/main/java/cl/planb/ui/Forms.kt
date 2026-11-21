package cl.planb.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cl.planb.model.*

@Composable
fun Campo(label: String, value: String, error: String? = null, change: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = change,
        label = { Text(label) },
        isError = error != null,
        trailingIcon = {
            if (error != null) Icon(Icons.Default.Error, contentDescription = "Error en el campo")
        },
        supportingText = { if (error != null) Text(error) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
}

@Composable
fun Editor(
    title: String,
    busy: Boolean,
    cancel: () -> Unit,
    save: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = { if (!busy) cancel() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(Modifier.fillMaxSize(), color = Crema) {
            Column(
                Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(onClick = cancel, enabled = !busy) { Text("Volver") }
                Text(title, style = MaterialTheme.typography.headlineMedium)
                content()
                Button(onClick = save, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                    Text(if (busy) "Guardando..." else "Guardar cambios")
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun LugarForm(
    lugar: Lugar,
    errores: Map<String, String>,
    busy: Boolean,
    mensaje: String?,
    cancel: () -> Unit,
    save: (Lugar) -> Unit,
) {
    var nombre by rememberSaveable { mutableStateOf(lugar.nombre) }
    var descripcion by rememberSaveable { mutableStateOf(lugar.descripcion) }
    var costo by rememberSaveable { mutableStateOf(lugar.costo.toString()) }
    var minutos by rememberSaveable { mutableStateOf(lugar.minutos.toString()) }
    var interior by rememberSaveable { mutableStateOf(lugar.interior) }
    var lat by rememberSaveable { mutableStateOf(lugar.latitud.toString()) }
    var lon by rememberSaveable { mutableStateOf(lugar.longitud.toString()) }
    Editor(
        if (lugar.id == null) "Un nuevo lugar" else "Editar lugar",
        busy,
        cancel,
        {
            save(
                lugar.copy(
                    nombre = nombre.trim(),
                    descripcion = descripcion.trim(),
                    costo = costo.toIntOrNull() ?: -1,
                    minutos = minutos.toIntOrNull() ?: -1,
                    interior = interior,
                    latitud = lat.toDoubleOrNull() ?: Double.NaN,
                    longitud = lon.toDoubleOrNull() ?: Double.NaN,
                ),
            )
        },
    ) {
        Campo("Nombre", nombre, errores["nombre"]) { nombre = it }
        Campo("¿Qué puedes hacer aquí?", descripcion, errores["descripcion"]) { descripcion = it }
        Campo("Costo estimado en pesos", costo, errores["costo"]) { costo = it }
        Campo("Duración en minutos", minutos, errores["minutos"]) { minutos = it }
        Row {
            Checkbox(checked = interior, onCheckedChange = { interior = it })
            Text("Es un lugar bajo techo", Modifier.padding(top = 12.dp))
        }
        Campo("Latitud", lat, errores["latitud"]) { lat = it }
        Campo("Longitud", lon, errores["longitud"]) { lon = it }
        mensaje?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

@Composable
fun PlanForm(
    plan: Plan,
    errores: Map<String, String>,
    busy: Boolean,
    mensaje: String?,
    cancel: () -> Unit,
    save: (Plan) -> Unit,
) {
    var titulo by rememberSaveable { mutableStateOf(plan.titulo) }
    var lugar by rememberSaveable { mutableStateOf(plan.lugarNombre) }
    var fecha by rememberSaveable { mutableStateOf(plan.fecha) }
    var estado by rememberSaveable { mutableStateOf(plan.estado) }
    var notas by rememberSaveable { mutableStateOf(plan.notas) }
    var valoracion by rememberSaveable { mutableStateOf(plan.valoracion.toString()) }
    Editor(
        if (plan.id == null) "Arma tu plan" else "Editar plan",
        busy,
        cancel,
        {
            save(
                plan.copy(
                    titulo = titulo.trim(),
                    lugarNombre = lugar.trim(),
                    fecha = fecha,
                    estado = estado,
                    notas = notas,
                    valoracion = valoracion.toIntOrNull() ?: -1,
                ),
            )
        },
    ) {
        Campo("Nombre de tu plan", titulo, errores["titulo"]) { titulo = it }
        Campo("Lugar", lugar, errores["lugarNombre"]) { lugar = it }
        Campo("Fecha: AAAA-MM-DD", fecha, errores["fecha"]) { fecha = it }
        Text("Estado")
        listOf("PENDIENTE", "REALIZADO", "CANCELADO").forEach { opcion ->
            FilterChip(
                selected = estado == opcion,
                onClick = {
                    estado = opcion
                    if (opcion != "REALIZADO") valoracion = "0"
                },
                label = { Text(opcion.lowercase().replaceFirstChar { it.uppercase() }) },
            )
        }
        Campo("Notas", notas, errores["notas"]) { notas = it }
        if (estado == "REALIZADO")
            Campo("Valoración de 0 a 5", valoracion, errores["valoracion"]) { valoracion = it }
        mensaje?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
