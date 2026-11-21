package cl.planb.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File
import java.net.URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProfileScreen(onConnectionChange: () -> Unit, onLocation: (Double, Double) -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("planb", 0) }
    var nombre by rememberSaveable { mutableStateOf(prefs.getString("nombre", "") ?: "") }
    var host by rememberSaveable {
        mutableStateOf(prefs.getString("host", "10.0.2.2") ?: "10.0.2.2")
    }
    var foto by rememberSaveable { mutableStateOf(prefs.getString("foto", null)) }
    var pendiente by rememberSaveable { mutableStateOf<String?>(null) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var cancelarUbicacion by remember { mutableStateOf<(() -> Unit)?>(null) }
    DisposableEffect(Unit) { onDispose { cancelarUbicacion?.invoke() } }
    val scope = rememberCoroutineScope()
    fun nuevaFoto(): File {
        val dir = File(context.filesDir, "fotos")
        dir.mkdirs()
        return File.createTempFile("perfil_", ".jpg", dir)
    }
    fun publicarFoto(path: String) {
        val anterior = foto
        foto = path
        prefs.edit().putString("foto", path).apply()
        if (anterior != null && anterior != path) {
            val file = File(anterior)
            if (file.parentFile?.canonicalFile == File(context.filesDir, "fotos").canonicalFile)
                file.delete()
        }
        mensaje = "Foto guardada en este teléfono"
    }
    val captura =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
            val path = pendiente
            if (ok && path != null) publicarFoto(path)
            else {
                path?.let { File(it).delete() }
                mensaje = "Captura cancelada"
            }
            pendiente = null
        }
    val permisoCamara =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
            if (ok) {
                try {
                    val f = nuevaFoto()
                    pendiente = f.path
                    captura.launch(
                        FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            f,
                        ),
                    )
                } catch (error: Exception) {
                    mensaje = "No se pudo abrir la cámara"
                }
            } else mensaje = "Permiso de cámara denegado; puedes elegir una foto de galería"
        }
    val galeria =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null)
                scope.launch {
                    try {
                        val path =
                            withContext(Dispatchers.IO) {
                                val f = nuevaFoto()
                                val input =
                                    context.contentResolver.openInputStream(uri)
                                        ?: error("Imagen no disponible")
                                input.use { source -> f.outputStream().use { source.copyTo(it) } }
                                f.path
                            }
                        publicarFoto(path)
                    } catch (error: Exception) {
                        mensaje = "No se pudo guardar la imagen"
                    }
                }
        }
    val ubicacion =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            permisos ->
            if (permisos.values.any { it }) {
                cancelarUbicacion?.invoke()
                mensaje = "Buscando ubicación..."
                cancelarUbicacion =
                    pedirUbicacion(context) { lat, lon, msg ->
                        mensaje = msg
                        if (lat != null && lon != null) onLocation(lat, lon)
                    }
            } else
                mensaje = "Permiso denegado. Puedes seguir usando la ciudad de referencia: Santiago"
        }
    Column(
        Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Tu espacio", style = MaterialTheme.typography.headlineLarge)
        Text("Tu nombre y tu foto se guardan solo en este dispositivo.")
        if (foto != null)
            AsyncImage(
                model = File(foto!!),
                contentDescription = "Foto de perfil",
                modifier = Modifier.size(110.dp),
            )
        Campo("¿Cómo te llamas?", nombre) { nombre = it }
        Button(
            onClick = {
                if (nombre.trim().length in 2..60) {
                    prefs.edit().putString("nombre", nombre.trim()).apply()
                    mensaje = "Nombre guardado"
                } else mensaje = "Escribe entre 2 y 60 caracteres"
            },
        ) {
            Text("Guardar nombre")
        }
        OutlinedButton(onClick = { permisoCamara.launch(Manifest.permission.CAMERA) }) {
            Text("Tomar fotografía")
        }
        OutlinedButton(onClick = { galeria.launch("image/*") }) { Text("Elegir desde galería") }
        OutlinedButton(
            onClick = {
                ubicacion.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ),
                )
            },
        ) {
            Text("Usar mi ubicación para el clima")
        }
        HorizontalDivider()
        Text("Conexión de laboratorio", style = MaterialTheme.typography.titleLarge)
        Text(
            "Emulador: 10.0.2.2. Teléfono: IP del computador en la misma red. Servicios en puertos 8081 y 8082.",
        )
        Campo("IP o nombre del computador (sin http://)", host) { host = it }
        Button(
            onClick = {
                val limpio = host.trim()
                val valido = runCatching {
                    URI("http://$limpio:8081/").host == limpio &&
                        limpio.isNotBlank() &&
                        !limpio.contains(":")
                }
                    .getOrDefault(false)
                if (valido) {
                    prefs.edit().putString("host", limpio).apply()
                    onConnectionChange()
                    mensaje = "Conexión actualizada"
                } else mensaje = "Escribe una IP o nombre válido, sin puerto ni barras"
            },
        ) {
            Text("Guardar conexión")
        }
        mensaje?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        Text(
            "Prototipo individual de laboratorio. Catálogo compartido de demostración; no utiliza cuentas ni datos personales remotos.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
