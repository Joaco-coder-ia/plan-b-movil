# Verificación de PLAN B

Fecha de preparación: 21 de noviembre de 2026.

## Comprobaciones ejecutadas

- Backend: ambos servicios compilados y empaquetados con JDK 21, Maven 3.9.9 y Spring Boot 3.5.14.
- Seis pruebas de integración aprobadas (tres por servicio): ciclo CRUD, datos inválidos y actualización de identificador inexistente.
- Servicios reales levantados en 8081 y 8082 con bases H2 en archivo. Prueba HTTP de creación, lectura, edición y eliminación aprobada; registros temporales retirados al terminar.
- Open-Meteo respondió correctamente a una consulta real con coordenadas de Santiago. La respuesta se registró en `evidencias/http.json`.
- Android: 22 pruebas aprobadas: 10 de reglas, 8 del ViewModel y 4 del repositorio/contratos Retrofit con MockWebServer.
- Cobertura JaCoCo de lógica: 115/121 líneas (95.04 %), 99/108 ramas (91,67 %). La tarea `verifyLogicCoverage` aprobó el umbral del 80 %.

## Alcance de la cobertura

El conjunto medido incluye `cl.planb.logic`, `cl.planb.viewmodel` y `RemoteRepository`, incluyendo clases generadas de sus corrutinas. No se afirma este porcentaje para toda la aplicación. UI, almacenamiento Room generado, modelos y funciones de dispositivo quedan fuera de ese conjunto y requieren verificación Android.

## Verificación de dispositivo y firma

- APK release compilado y firma verificada con apksigner; certificado en `evidencias/firma-apk.txt`.
- Instalado y abierto en emulador Android 35. Navegación, carga de lugares/clima, selección y filtro de favoritos y creación de un plan comprobados manualmente.
- Capturas reales: `01-inicio.png`, `02-favoritos.png`, `03-planes.png`.
- Cámara, galería y GPS están implementados y compilados, pero no se declara una prueba completa de esos flujos en dispositivo físico.
- GitHub Actions ejecuta nuevamente backend y Android al subir cambios; consultar el resultado en GitHub, no confundir configuración con ejecución aprobada.

## Límites que siguen aplicando

- Prototipo de laboratorio sin autenticación remota: no publicar servicios abiertos en Internet.
- No se ensayaron todos los dispositivos físicos ni versiones de Android.
- No hay sincronización automática de favoritos: son copias locales explícitas.
- No se afirma haber creado un tablero Trello sin conexión confirmada ni haber realizado la entrega en AVA.
- La cobertura y las pruebas no reemplazan el ensayo individual del alumno.
