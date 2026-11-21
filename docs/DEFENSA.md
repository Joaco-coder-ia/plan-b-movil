# Defender PLAN B en 15 minutos

Este documento es para practicar antes. La pauta indica defensa individual sin IA ni recursos de apoyo.

## Recorrido recomendado

1. **0:00–1:30**: iniciar servicios y app. Explicar JDK, SDK, puertos y dirección del emulador.
2. **1:30–3:00**: Screen dibuja, ViewModel coordina, Repository llama a Retrofit. Controller recibe, Service valida/coordina y Repository JPA persiste.
3. **3:00–5:00**: crear un lugar, editar costo y duración, crear un plan y cambiar su estado. Mostrar respuesta remota y persistencia tras reiniciar servicios.
4. **5:00–6:00**: consultar clima, mostrar los parámetros Retrofit y explicar la diferencia entre API ajena y servicios propios.
5. **6:00–7:00**: mostrar favorito offline, foto y ubicación con permisos.
6. **7:00–9:00**: ejecutar pruebas y abrir el informe de cobertura. Explicar que el porcentaje corresponde a la lógica declarada, no a todas las pantallas.
7. **9:00–10:00**: mostrar APK y resultado de apksigner; explicar JKS, alias y configuración privada.
8. **10:00–15:00**: modificación solicitada por el profesor, prueba y explicación. El docente controla el tiempo y puede cambiar este orden.

## Explicación del algoritmo

“Primero filtro lugares cuya duración cabe en mis minutos libres. Después descarto los que superan mi presupuesto. Si llueve, conservo los interiores. Ordeno por costo y después por duración. Esto está en una función pequeña y se prueba sin Android ni conexión.”

## Ejercicios que debes poder realizar

- Cambiar duración mínima de 5 a 10 minutos: regla Android, anotación del backend y pruebas de límites.
- Agregar un filtro gratuito: estado UI, argumento del filtro y caso de prueba.
- Agregar `categoria` a lugares: entidad, modelo Kotlin, formulario y pruebas; explicar cómo actualizar una base existente.
- Cambiar el umbral de lluvia: función de clima y pruebas.
- Agregar un endpoint que filtre lugares interiores: Repository/Service/Controller, Retrofit y prueba HTTP.
- Simular error 500 con MockWebServer y comprobar que el ViewModel conserva la última lista.

## Preguntas cortas

**¿Por qué dos servicios?** Lugares administra el catálogo; planes administra la agenda y valoraciones. Cada uno arranca y guarda datos por separado.

**¿Por qué el plan guarda el nombre del lugar?** Es una copia histórica. No necesita consultar el catálogo para mostrar planes anteriores y borrar un lugar no borra un plan.

**¿Qué pasa sin internet?** Favoritos siguen disponibles en Room. Las operaciones remotas muestran errores y pueden reintentarse. No existe una cola automática de sincronización.

**¿Qué mockeas?** El repositorio en pruebas del ViewModel; el servidor HTTP mediante MockWebServer en las del repositorio. No se llama a Open-Meteo en cada prueba.

**¿Es una aplicación multiusuario segura?** No. Es un prototipo de laboratorio compartido sin autenticación remota. La foto y el nombre se guardan localmente.

**¿Qué cambiaste tú?** Explica solo lo que realmente estudiaste, ejecutaste o modificaste. Los commits y los apuntes no reemplazan comprender el código.
