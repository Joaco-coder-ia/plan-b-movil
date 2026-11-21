# Entender PLAN B sin memorizarlo

## Un recorrido completo

1. En `LugarForm`, escribo datos. Las variables de Compose conservan el texto del formulario.
2. Al guardar, creo un objeto `Lugar` y llamo a `PlanBViewModel.guardar`.
3. `Reglas.lugar` devuelve un mapa: el nombre del campo y su mensaje de error. Si hay errores, no envío la solicitud.
4. El ViewModel marca `cargando`, y la interfaz desactiva el botón mientras trabaja.
5. `RemoteRepository` decide entre crear (sin id) y actualizar (con id).
6. Retrofit convierte el objeto a JSON y llama al endpoint del servicio de lugares.
7. Spring valida el cuerpo, el Controller llama al Service y este utiliza el Repository JPA.
8. JPA guarda en H2 y Spring devuelve JSON. El ViewModel vuelve a consultar y actualiza StateFlow.
9. Compose observa el estado y muestra la lista actualizada.

## Archivos importantes

| Archivo | Qué resuelve |
|---|---|
| MainActivity | Inicia Android y monta la interfaz Compose. |
| Models | Datos de lugar, plan, clima y estado visible. |
| Reglas | Validaciones y sugerencias; no depende de Android ni de internet. |
| Apis | Declara las rutas HTTP y el contrato Repository. |
| RemoteRepository | Envía y recupera datos mediante las APIs Retrofit. |
| PlanBViewModel | Coordina validaciones, operaciones, carga y mensajes. |
| LocalStore | Tabla favoritos, DAO y conexión Room. |
| PlanBApp | Navegación de pestañas, listas, filtros y apertura de formularios. |
| Forms | Campos y editores de lugares/planes. |
| ProfileScreen | Nombre local, foto, permisos, ubicación y dirección del computador. |
| DeviceTools | Solicita una ubicación con límite de espera y permite cancelar. |
| Theme | Colores y estilo general Material 3. |
| Lugar/Plan.java | Entidades persistentes y restricciones del backend. |
| Service.java | Operaciones de negocio y respuesta cuando no existe el registro. |
| Repository.java | Operaciones de base de datos implementadas por Spring Data. |
| Controller.java | Rutas REST y códigos de respuesta. |
| Application.java | Punto de entrada de cada servicio Spring Boot. |

## Conceptos esenciales

- `val`: referencia que no reasigno. `var`: referencia que sí puedo cambiar.
- `data class`: agrupa datos; `copy` crea una versión con algunos campos diferentes.
- `null`: ausencia de valor. `?` permite null; `?.` ejecuta solo si existe; `?:` proporciona un valor alternativo.
- `suspend`: función que puede suspenderse sin bloquear el hilo mientras espera.
- `viewModelScope.launch`: inicia trabajo ligado a la vida del ViewModel.
- `StateFlow`: estado observable. No guarda en disco por sí mismo.
- `Room`: persistencia local del teléfono. `H2`: persistencia remota de los servicios.
- `@GET`, `@POST`, `@PUT`, `@DELETE`: operaciones HTTP que Retrofit implementa.
- `@Entity`: una clase que representa filas de una tabla.
- `@Valid`: solicita validar el objeto recibido según sus anotaciones.
- `MockWebServer`: servidor local de pruebas con respuestas controladas; no es el backend de producción.
- `JKS`: archivo de claves para firmar; no es la contraseña ni el APK.

## Primeros cambios para practicar

Empieza con un texto o un color. Después cambia una validación y su prueba. Luego sigue un campo desde el formulario hasta la base de datos. Finalmente añade un filtro. Haz un cambio pequeño, ejecuta sus pruebas y explícalo con tus palabras antes de continuar.

En la copia comentada, cada línea de código tiene su explicación encima. Los comentarios no cambian las instrucciones que ejecuta la aplicación. Los wrappers oficiales y archivos binarios se conservan sin añadir texto.
