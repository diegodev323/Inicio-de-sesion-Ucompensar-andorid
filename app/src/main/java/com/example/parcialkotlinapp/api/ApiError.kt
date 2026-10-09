package com.example.parcialkotlinapp.api

import com.google.gson.JsonParser
import retrofit2.Response

/**
 * Interpreta el cuerpo de error que devuelve la API .NET, por ejemplo:
 *
 *   HTTP 400  { "mensaje": "Ya existe un usuario con ese correo." }
 *
 * y devuelve ese mensaje para mostrarlo al usuario. Solo cae en un mensaje
 * genérico cuando el servidor no envía un mensaje controlado.
 */
object ApiError {

    fun mensaje(response: Response<*>): String {
        val cuerpo = try {
            response.errorBody()?.string()
        } catch (e: Exception) {
            null
        }
        return mensajeDesdeJson(cuerpo) ?: mensajePorCodigo(response.code())
    }

    /** Extrae "mensaje" (formato de la API) o, si no está, el formato ProblemDetails. */
    fun mensajeDesdeJson(cuerpo: String?): String? {
        if (cuerpo.isNullOrBlank()) return null
        return try {
            val json = JsonParser.parseString(cuerpo)
            if (!json.isJsonObject) return null
            val obj = json.asJsonObject

            val mensaje = obj.get("mensaje")
            if (mensaje != null && mensaje.isJsonPrimitive && mensaje.asString.isNotBlank()) {
                return mensaje.asString
            }

            // Validaciones automáticas de ASP.NET: { "errors": { "Campo": ["texto"] } }
            val errores = obj.get("errors")
            if (errores != null && errores.isJsonObject) {
                for ((_, lista) in errores.asJsonObject.entrySet()) {
                    if (lista.isJsonArray && lista.asJsonArray.size() > 0) {
                        return lista.asJsonArray[0].asString
                    }
                }
            }

            val titulo = obj.get("title")
            if (titulo != null && titulo.isJsonPrimitive) titulo.asString else null
        } catch (e: Exception) {
            null
        }
    }

    fun mensajePorCodigo(codigo: Int): String = when (codigo) {
        400 -> "Los datos enviados no son válidos."
        401 -> "Correo o contraseña incorrectos."
        404 -> "No se encontró el recurso solicitado."
        in 500..599 -> "El servidor tuvo un problema (código $codigo). Intente más tarde."
        else -> "No se pudo completar la operación (código $codigo)."
    }
}
