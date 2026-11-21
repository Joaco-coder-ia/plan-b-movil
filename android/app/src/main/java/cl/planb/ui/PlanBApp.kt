package cl.planb.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.planb.data.*
import cl.planb.logic.Reglas
import cl.planb.model.*
import cl.planb.viewmodel.PlanBViewModel
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanBApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("planb", 0) }
    var revision by rememberSaveable { mutableIntStateOf(0) }
    val host = remember(revision) { prefs.getString("host", "10.0.2.2") ?: "10.0.2.2" }
    val repo =
        remember(host) {
            val client =
                OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build()
            fun retrofit(url: String) =
                Retrofit.Builder()
                    .baseUrl(url)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            RemoteRepository(
                retrofit("http://$host:8081/").create(LugaresApi::class.java),
                retrofit("http://$host:8082/").create(PlanesApi::class.java),
                retrofit("https://api.open-meteo.com/").create(WeatherApi::class.java),
            )
        }
    val vm: PlanBViewModel =
        viewModel(
            key = host,
            factory =
                object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T =
                        PlanBViewModel(repo) as T
                },
        )
    val state by vm.state.collectAsState()
    val dao = remember { LocalStore.abrir(context).favoritos() }
    val favoritos by dao.observar().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var minutos by rememberSaveable { mutableStateOf("60") }
    var presupuesto by rememberSaveable { mutableStateOf("5000") }
    var lat by rememberSaveable { mutableStateOf(prefs.getString("lat", "-33.45")!!.toDouble()) }
    var lon by rememberSaveable { mutableStateOf(prefs.getString("lon", "-70.66")!!.toDouble()) }
    var lugarEdit by remember { mutableStateOf<Lugar?>(null) }
    var planEdit by remember { mutableStateOf<Plan?>(null) }
    var borrarLugar by remember { mutableStateOf<Lugar?>(null) }
    var borrarPlan by remember { mutableStateOf<Plan?>(null) }
    var soloFavoritos by rememberSaveable { mutableStateOf(false) }
    var errorLocal by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(host) { vm.cargar() }
    LaunchedEffect(lat, lon, host) { vm.consultarClima(lat, lon) }
    fun favorito(l: Lugar) {
        scope.launch {
            try {
                val id = l.id ?: return@launch
                if (favoritos.any { it.id == id }) dao.eliminar(id)
                else
                    dao.guardar(
                        Favorito(id, l.nombre, l.descripcion, l.costo, l.minutos, l.interior),
                    )
            } catch (error: Exception) {
                errorLocal = "No se pudo actualizar el favorito local"
            }
        }
    }
    fun planear(l: Lugar) {
        vm.limpiarErrores()
        planEdit =
            Plan(
                titulo = "Visitar ${l.nombre}",
                lugarNombre = l.nombre,
                fecha = LocalDate.now().toString(),
            )
    }
    PlanBTheme {
        Scaffold(
            containerColor = Crema,
            topBar = {
                TopAppBar(
                    title = { Text("PLAN B", fontWeight = FontWeight.Black) },
                    actions = {
                        IconButton(onClick = { vm.cargar() }, enabled = !state.cargando) {
                            Icon(Icons.Default.Refresh, contentDescription = "Actualizar datos")
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = Bosque,
                            titleContentColor = Lima,
                            actionIconContentColor = Lima,
                        ),
                )
            },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    val nombres = listOf("Descubrir", "Lugares", "Mis planes", "Perfil")
                    val iconos =
                        listOf(
                            Icons.Default.Explore,
                            Icons.Default.Place,
                            Icons.Default.Event,
                            Icons.Default.Person,
                        )
                    nombres.forEachIndexed { i, n ->
                        NavigationBarItem(
                            selected = tab == i,
                            onClick = { tab = i },
                            icon = { Icon(iconos[i], contentDescription = n) },
                            label = { Text(n) },
                        )
                    }
                }
            },
        ) { padding ->
            Column(Modifier.padding(padding)) {
                if (state.cargando) LinearProgressIndicator(Modifier.fillMaxWidth())
                AnimatedVisibility(visible = state.mensaje != null || errorLocal != null) {
                    Text(
                        errorLocal ?: state.mensaje.orEmpty(),
                        Modifier.fillMaxWidth().padding(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Crossfade(targetState = tab, label = "Cambiar sección") { pagina ->
                    when (pagina) {
                        0 ->
                            LazyColumn(
                                contentPadding = PaddingValues(20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                item {
                                    Column(
                                        Modifier.fillMaxWidth()
                                            .background(Bosque, RoundedCornerShape(24.dp))
                                            .padding(24.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        Text(
                                            "UN RATO LIBRE.\nUN BUEN PLAN.",
                                            color = Lima,
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.Black,
                                        )
                                        Text(
                                            "Encuentra una pausa que cabe en tu día.",
                                            color = Crema,
                                        )
                                    }
                                }
                                item {
                                    Text(
                                        "¿Qué tienes disponible?",
                                        style = MaterialTheme.typography.titleLarge,
                                    )
                                    Campo(
                                        "Minutos libres",
                                        minutos,
                                        if ((minutos.toIntOrNull() ?: 0) <= 0)
                                            "Usa un número positivo"
                                        else null,
                                    ) {
                                        minutos = it
                                    }
                                    Campo(
                                        "Presupuesto en pesos",
                                        presupuesto,
                                        if ((presupuesto.toIntOrNull() ?: -1) < 0)
                                            "Usa un número desde cero"
                                        else null,
                                    ) {
                                        presupuesto = it
                                    }
                                }
                                item {
                                    Card(colors = CardDefaults.cardColors(containerColor = Lima)) {
                                        Column(Modifier.padding(16.dp)) {
                                            Text(
                                                state.clima?.let {
                                                    "${it.temperatura} °C · ${it.descripcion}"
                                                } ?: state.errorClima ?: "Consultando clima...",
                                                fontWeight = FontWeight.Bold,
                                            )
                                            Text(
                                                "Zona: ${"%.3f".format(lat)}, ${"%.3f".format(lon)} · Open-Meteo",
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                            TextButton(onClick = { vm.consultarClima(lat, lon) }) {
                                                Text("Actualizar clima")
                                            }
                                        }
                                    }
                                }
                                val sugeridos =
                                    Reglas.sugerir(
                                        state.lugares,
                                        minutos.toIntOrNull() ?: 0,
                                        presupuesto.toIntOrNull() ?: -1,
                                        state.clima?.lluvia ?: true,
                                    )
                                item {
                                    Text(
                                        "Tu siguiente pausa",
                                        style = MaterialTheme.typography.titleLarge,
                                    )
                                }
                                if (sugeridos.isEmpty())
                                    item {
                                        Text(
                                            "No hay coincidencias. Agrega un lugar o ajusta tiempo y presupuesto. Sin clima, mostramos solo interiores.",
                                        )
                                    }
                                items(sugeridos, key = { it.id ?: 0 }) { l ->
                                    LugarCard(
                                        l,
                                        favoritos.any { it.id == l.id },
                                        { favorito(l) },
                                        { planear(l) },
                                    )
                                }
                            }
                        1 ->
                            LazyColumn(
                                contentPadding = PaddingValues(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                item {
                                    Text(
                                        "Lugares con posibilidades",
                                        style = MaterialTheme.typography.headlineMedium,
                                    )
                                }
                                item {
                                    Button(
                                        onClick = {
                                            vm.limpiarErrores()
                                            lugarEdit = Lugar(latitud = lat, longitud = lon)
                                        },
                                        enabled = !state.cargando,
                                    ) {
                                        Text("+ Agregar lugar")
                                    }
                                }
                                item {
                                    FilterChip(
                                        selected = soloFavoritos,
                                        onClick = { soloFavoritos = !soloFavoritos },
                                        label = { Text("Favoritos sin conexión") },
                                    )
                                }
                                if (soloFavoritos) {
                                    item {
                                        Text(
                                            "Copias guardadas en este teléfono. Pueden diferir del catálogo actualizado.",
                                        )
                                    }
                                    if (favoritos.isEmpty())
                                        item {
                                            Text(
                                                "Aún no tienes favoritos. Guarda uno usando la estrella.",
                                            )
                                        }
                                    items(favoritos, key = { it.id }) { f ->
                                        LugarCard(
                                            Lugar(
                                                f.id,
                                                f.nombre,
                                                f.descripcion,
                                                f.costo,
                                                f.minutos,
                                                f.interior,
                                            ),
                                            true,
                                            { favorito(Lugar(id = f.id)) },
                                            { planear(Lugar(nombre = f.nombre)) },
                                        )
                                    }
                                } else {
                                    if (state.lugares.isEmpty())
                                        item {
                                            Text(
                                                "Tu catálogo está vacío o no pudo cargarse. Agrega un lugar o revisa la conexión en Perfil.",
                                            )
                                        }
                                    items(state.lugares, key = { it.id ?: 0 }) { l ->
                                        LugarCard(
                                            l,
                                            favoritos.any { it.id == l.id },
                                            { favorito(l) },
                                            { planear(l) },
                                            edit = {
                                                vm.limpiarErrores()
                                                lugarEdit = l
                                            },
                                            delete = { borrarLugar = l },
                                            busy = state.cargando,
                                        )
                                    }
                                }
                            }
                        2 ->
                            LazyColumn(
                                contentPadding = PaddingValues(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                item {
                                    Text(
                                        "Pequeños planes.\nGrandes pausas.",
                                        style = MaterialTheme.typography.headlineMedium,
                                    )
                                }
                                item {
                                    Button(
                                        onClick = {
                                            vm.limpiarErrores()
                                            planEdit = Plan(fecha = LocalDate.now().toString())
                                        },
                                        enabled = !state.cargando,
                                    ) {
                                        Text("+ Crear plan")
                                    }
                                }
                                if (state.planes.isEmpty())
                                    item {
                                        Text(
                                            "Todavía no hay planes. Elige un lugar o crea el tuyo.",
                                        )
                                    }
                                items(state.planes, key = { it.id ?: 0 }) { p ->
                                    Card {
                                        Column(
                                            Modifier.padding(18.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Text(
                                                p.titulo,
                                                style = MaterialTheme.typography.titleLarge,
                                            )
                                            Text("${p.lugarNombre} · ${p.fecha}")
                                            Text(
                                                p.estado,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                            )
                                            if (p.notas.isNotBlank()) Text(p.notas)
                                            if (p.estado == "REALIZADO")
                                                Text("Tu valoración: ${p.valoracion}/5")
                                            Row {
                                                TextButton(
                                                    onClick = {
                                                        vm.limpiarErrores()
                                                        planEdit = p
                                                    },
                                                    enabled = !state.cargando,
                                                ) {
                                                    Text("Editar")
                                                }
                                                TextButton(
                                                    onClick = { borrarPlan = p },
                                                    enabled = !state.cargando,
                                                ) {
                                                    Text("Eliminar")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        else ->
                            ProfileScreen(
                                onConnectionChange = { revision++ },
                                onLocation = { a, b ->
                                    lat = a
                                    lon = b
                                    prefs
                                        .edit()
                                        .putString("lat", a.toString())
                                        .putString("lon", b.toString())
                                        .apply()
                                },
                            )
                    }
                }
            }
        }
        lugarEdit?.let { l ->
            LugarForm(l, state.errores, state.cargando, state.mensaje, { lugarEdit = null }) {
                vm.guardar(it) { lugarEdit = null }
            }
        }
        planEdit?.let { p ->
            PlanForm(p, state.errores, state.cargando, state.mensaje, { planEdit = null }) {
                vm.guardar(it) { planEdit = null }
            }
        }
        if (borrarLugar != null || borrarPlan != null)
            AlertDialog(
                onDismissRequest = {
                    borrarLugar = null
                    borrarPlan = null
                },
                title = { Text("¿Eliminar este registro?") },
                text = {
                    Text(
                        "Se eliminará del servidor. Los favoritos locales se conservan como copias independientes.",
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            borrarLugar?.id?.let(vm::borrarLugar)
                            borrarPlan?.id?.let(vm::borrarPlan)
                            borrarLugar = null
                            borrarPlan = null
                        },
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            borrarLugar = null
                            borrarPlan = null
                        },
                    ) {
                        Text("Cancelar")
                    }
                },
            )
    }
}

@Composable
fun LugarCard(
    l: Lugar,
    favorito: Boolean,
    onFavorite: () -> Unit,
    onPlan: () -> Unit,
    edit: (() -> Unit)? = null,
    delete: (() -> Unit)? = null,
    busy: Boolean = false,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(l.nombre, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onFavorite) {
                    Icon(
                        if (favorito) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription =
                            if (favorito) "Quitar favorito" else "Guardar favorito",
                    )
                }
            }
            Text(l.descripcion)
            Text(
                "${l.minutos} min · $${l.costo} · ${if(l.interior) "Bajo techo" else "Al aire libre"}",
                fontWeight = FontWeight.Bold,
            )
            Button(onClick = onPlan, enabled = !busy) { Text("Este es mi plan") }
            if (edit != null && delete != null)
                Row {
                    TextButton(onClick = edit, enabled = !busy) { Text("Editar") }
                    TextButton(onClick = delete, enabled = !busy) { Text("Eliminar") }
                }
        }
    }
}
