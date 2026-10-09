package com.example.parcialkotlinapp.util

import java.util.Calendar
import java.util.Locale

object FechaUtils {

    /**
     * Edad exacta: considera año, mes y día (no solo la resta de años).
     * [mes] va de 1 a 12.
     */
    fun calcularEdad(
        anioNac: Int, mesNac: Int, diaNac: Int,
        anioHoy: Int, mesHoy: Int, diaHoy: Int
    ): Int {
        var edad = anioHoy - anioNac
        if (mesHoy < mesNac || (mesHoy == mesNac && diaHoy < diaNac)) {
            edad--
        }
        return edad
    }

    /** Edad respecto a la fecha actual del dispositivo. */
    fun calcularEdadHoy(anioNac: Int, mesNac: Int, diaNac: Int): Int {
        val hoy = Calendar.getInstance()
        return calcularEdad(
            anioNac, mesNac, diaNac,
            hoy.get(Calendar.YEAR),
            hoy.get(Calendar.MONTH) + 1,
            hoy.get(Calendar.DAY_OF_MONTH)
        )
    }

    /** Formato que espera la API: yyyy-MM-dd. [mes] va de 1 a 12. */
    fun formatoApi(anio: Int, mes: Int, dia: Int): String =
        String.format(Locale.US, "%04d-%02d-%02d", anio, mes, dia)

    /** Formato legible para mostrar al usuario: dd/MM/yyyy. */
    fun formatoVisible(anio: Int, mes: Int, dia: Int): String =
        String.format(Locale.US, "%02d/%02d/%04d", dia, mes, anio)

    /** Saludo según la hora (0-23) del dispositivo. */
    fun saludoSegunHora(hora: Int): String = when (hora) {
        in 5..11 -> "Buenos días"
        in 12..17 -> "Buenas tardes"
        else -> "Buenas noches"
    }
}
