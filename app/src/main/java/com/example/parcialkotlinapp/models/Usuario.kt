package com.example.parcialkotlinapp.models

data class Usuario(
    val id: Int,
    val nombre: String,
    val apellido: String,
    val correo: String,
    val fechaNacimiento: String? = null,
    val edad: Int = 0,
    val universidad: String? = null,
    val semestre: Int = 0,
    val activo: Boolean = true,
    val fechaCreacion: String? = null
)
