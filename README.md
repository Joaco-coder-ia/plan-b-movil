# PLAN B

**Un rato libre. Un buen plan.**

Aplicación Android que organiza lugares y planes personales, y filtra opciones por tiempo, presupuesto y clima. Proyecto académico de Joaquín Alberto González Sánchez para Desarrollo de Aplicaciones Móviles. La modalidad individual y el cambio de contexto deben estar acordados con el docente.

## Funcionalidades

- Descubrir: filtros de minutos/presupuesto y clima actual de Open-Meteo.
- Lugares: crear, consultar, actualizar y eliminar lugares remotos; confirmar antes de borrar.
- Mis planes: crear, consultar, editar y borrar planes; marcar realizados/cancelados y valorarlos.
- Favoritos: copias locales en Room para leer sin conexión; se identifican como copias que pueden estar desactualizadas.
- Perfil local: nombre, cámara, galería y ubicación para consultar el clima de la zona.
- Feedback: errores por campo, estados de carga, mensajes y transiciones.

No se usa IA para decidir sugerencias: las reglas están en `Reglas.sugerir` y se pueden explicar y modificar. Los lugares se introducen por el usuario; los datos de demostración son ficticios.

## Arquitectura

```text
Compose -> PlanBViewModel -> Repository -> Retrofit -> Lugares (8081) -> H2 en archivo
                                                -> Planes  (8082) -> H2 en archivo
                                                -> Open-Meteo (HTTPS)
Favoritos -> Room (SQLite en el dispositivo)
```

Son dos servicios Spring Boot ejecutables por separado, con bases independientes. Los planes conservan el nombre del lugar como una instantánea: borrar/renombrar el catálogo no borra el historial de planes. No se implementan gateway, descubrimiento de servicios ni transacciones distribuidas.

## Requisitos

- JDK 21 para backend y compilación; Android Studio compatible con AGP 8.9.2.
- SDK Android 35, Build Tools 35.0.0; teléfono/emulador API 26 o superior.
- Acceso a Maven Central, Google Maven y Open-Meteo para la primera preparación.

## Ejecutar

1. En `backend`: `mvnw.cmd clean package` (Windows) o `./mvnw clean package`.
2. En Windows: `powershell -ExecutionPolicy Bypass -File scripts/iniciar-servicios.ps1`. En otro sistema, ejecutar cada JAR desde su propia carpeta: `java -jar target/lugares-1.0.0.jar` y `java -jar target/planes-1.0.0.jar`.
3. Comprobar `http://localhost:8081/api/lugares` y `http://localhost:8082/api/planes`.
4. Opcional: ejecutar `scripts/datos-demo.ps1` para crear tres lugares ficticios sin repetirlos.
5. Abrir **android/** en Android Studio, sincronizar y ejecutar `app`.
6. En Perfil: emulador = `10.0.2.2`; teléfono = IP LAN del computador. Ambos dispositivos deben poder conectarse a los puertos 8081 y 8082. No abrir estos servicios a Internet.
7. El clima usa Santiago como referencia inicial; “Usar mi ubicación” solicita permiso y cambia las coordenadas.

## Endpoints

| Servicio | Método | Ruta | Función |
|---|---|---|---|
| Lugares, 8081 | GET/POST | `/api/lugares` | Listar/crear |
| Lugares, 8081 | GET/PUT/DELETE | `/api/lugares/{id}` | Consultar/editar/borrar |
| Planes, 8082 | GET/POST | `/api/planes` | Listar/crear |
| Planes, 8082 | GET/PUT/DELETE | `/api/planes/{id}` | Consultar/editar/borrar |
| Open-Meteo | GET | `https://api.open-meteo.com/v1/forecast` | Clima con latitude, longitude y current |

Datos externos: [documentación Open-Meteo](https://open-meteo.com/en/docs), atribución Open-Meteo y sus proveedores. Se usa el servicio público para este prototipo académico no comercial. Si falla, la app lo informa y aplica el filtro de interiores; no presenta clima inventado.

## Pruebas y cobertura

En Android: `gradlew.bat testDebugUnitTest logicCoverage verifyLogicCoverage`. El último objetivo exige un mínimo del 80 % de líneas del conjunto lógico: `logic`, `viewmodel` y `RemoteRepository`. Incluye toda la lógica de sugerencias, validación, coordinación de estado y repositorio remoto. No es un porcentaje de toda la app: UI, modelos, DAO generado, recursos y funciones de plataforma se validan por compilación y pruebas de dispositivo.

El reporte HTML está en `app/build/reports/jacoco/logicCoverage/html/index.html`. Los servicios tienen pruebas de integración con MockMvc y H2: CRUD, validaciones y errores 404. Ver `docs/VERIFICACION.md` para resultados reales, limitaciones y pendientes.

## APK y firma

La entrega privada incluye el APK, la llave académica JKS y su configuración. No subir contraseñas ni claves privadas al repositorio público. Para reconstruir: copiar `android/signing.properties.example` a `android/signing.properties`, completar rutas/credenciales y ejecutar `gradlew.bat assembleRelease`.

## Alcance de laboratorio

El catálogo y los planes son un espacio compartido de demostración **sin autenticación**. No se ofrece aislamiento por cuenta. El perfil/foto son locales. Los servicios usan HTTP en red privada y H2 persistente; no se despliegan como sistema público de producción. Antes de un despliegue real hacen falta autenticación, autorización, HTTPS, migraciones y política de datos.

La recomendación comprueba duración, costo y lluvia; no calcula tiempo de desplazamiento ni garantiza apertura, precio o seguridad de un lugar. La coordenada del lugar es informativa. La ubicación del perfil determina la zona del clima.

## Entrega y aprendizaje

- `docs/DEFENSA.md`: recorrido y ejercicios para una defensa de 15 minutos.
- `docs/MATRIZ_RUBRICA.csv`: requisito y evidencia asociada.
- `docs/PLANIFICACION.csv`: tareas reales y propuestas para trasladar a Trello; no simula un historial colaborativo.
- Versión comentada en ZIP independiente: mismo código, comentarios para estudio.

Se consultaron los dos proyectos PRACTIKA recibidos como referencia de organización y se escribió esta implementación para el nuevo contexto. No se atribuye participación retrospectiva en el grupo anterior ni se inventan commits de compañeros.
