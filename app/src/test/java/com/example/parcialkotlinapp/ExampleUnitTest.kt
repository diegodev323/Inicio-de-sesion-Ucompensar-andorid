package com.example.parcialkotlinapp

import com.example.parcialkotlinapp.api.ApiError
import com.example.parcialkotlinapp.util.FechaUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExampleUnitTest {

    // ---------- Edad (día, mes y año) ----------
    @Test
    fun edad_cumpleanosYaPaso() {
        // Hoy 2026-10-08, nació 2000-03-15 -> 26
        assertEquals(26, FechaUtils.calcularEdad(2000, 3, 15, 2026, 10, 8))
    }

    @Test
    fun edad_cumpleanosTodavia_noLlega() {
        // Hoy 2026-10-08, nació 2000-12-25 -> aún tiene 25
        assertEquals(25, FechaUtils.calcularEdad(2000, 12, 25, 2026, 10, 8))
    }

    @Test
    fun edad_mismoMesDiaAnterior() {
        assertEquals(25, FechaUtils.calcularEdad(2000, 10, 9, 2026, 10, 8))
    }

    @Test
    fun edad_elDiaDelCumpleanos() {
        assertEquals(26, FechaUtils.calcularEdad(2000, 10, 8, 2026, 10, 8))
    }

    // ---------- Saludo por horario ----------
    @Test
    fun saludo_porHora() {
        assertEquals("Buenas noches", FechaUtils.saludoSegunHora(0))
        assertEquals("Buenas noches", FechaUtils.saludoSegunHora(4))
        assertEquals("Buenos días", FechaUtils.saludoSegunHora(5))
        assertEquals("Buenos días", FechaUtils.saludoSegunHora(11))
        assertEquals("Buenas tardes", FechaUtils.saludoSegunHora(12))
        assertEquals("Buenas tardes", FechaUtils.saludoSegunHora(17))
        assertEquals("Buenas noches", FechaUtils.saludoSegunHora(18))
        assertEquals("Buenas noches", FechaUtils.saludoSegunHora(23))
    }

    // ---------- Formato de fecha ----------
    @Test
    fun formatoFecha() {
        assertEquals("2000-03-05", FechaUtils.formatoApi(2000, 3, 5))
        assertEquals("05/03/2000", FechaUtils.formatoVisible(2000, 3, 5))
    }

    // ---------- Mensajes de error de la API ----------
    @Test
    fun error_mensajeControlado() {
        val json = """{"mensaje":"Ya existe un usuario con ese correo."}"""
        assertEquals(
            "Ya existe un usuario con ese correo.",
            ApiError.mensajeDesdeJson(json)
        )
    }

    @Test
    fun error_problemDetails() {
        val json = """{"title":"One or more validation errors occurred.","errors":{"Semestre":["El semestre debe estar entre 1 y 10."]}}"""
        assertEquals(
            "El semestre debe estar entre 1 y 10.",
            ApiError.mensajeDesdeJson(json)
        )
    }

    @Test
    fun error_sinCuerpo() {
        assertNull(ApiError.mensajeDesdeJson(null))
        assertNull(ApiError.mensajeDesdeJson(""))
        assertNull(ApiError.mensajeDesdeJson("<html>500</html>"))
    }
}
