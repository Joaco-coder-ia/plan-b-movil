package cl.planb.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat

fun pedirUbicacion(context: Context, result: (Double?, Double?, String) -> Unit): () -> Unit {
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val handler = Handler(Looper.getMainLooper())
    var terminado = false
    lateinit var listener: LocationListener
    lateinit var timeout: Runnable
    fun finalizar(location: Location?, mensaje: String) {
        if (terminado) return
        terminado = true
        manager.removeUpdates(listener)
        handler.removeCallbacks(timeout)
        result(location?.latitude, location?.longitude, mensaje)
    }
    listener =
        object : LocationListener {
            override fun onLocationChanged(location: Location) {
                finalizar(location, "Ubicación actualizada")
            }

            override fun onProviderEnabled(provider: String) {}

            override fun onProviderDisabled(provider: String) {}

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }
    timeout = Runnable {
        finalizar(
            null,
            "No se obtuvo ubicación. Activa la ubicación del teléfono y vuelve a intentar.",
        )
    }
    try {
        val fine =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        val coarse =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        val provider =
            when {
                fine && manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ->
                    LocationManager.GPS_PROVIDER
                (fine || coarse) && manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ->
                    LocationManager.NETWORK_PROVIDER
                else -> null
            }
        if (provider == null)
            finalizar(null, "Activa ubicación y concede permiso para consultar el clima de tu zona")
        else {
            handler.postDelayed(timeout, 15000)
            manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
        }
    } catch (error: Exception) {
        finalizar(null, "No se pudo acceder a la ubicación")
    }
    return {
        terminado = true
        manager.removeUpdates(listener)
        handler.removeCallbacks(timeout)
    }
}
