package com.example.parcialkotlinapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.parcialkotlinapp.api.ApiClient
import com.example.parcialkotlinapp.api.ApiError
import com.example.parcialkotlinapp.models.LoginRequest
import com.example.parcialkotlinapp.models.Usuario
import com.example.parcialkotlinapp.util.aplicarInsetsDelSistema
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        findViewById<android.view.View>(R.id.rootLogin).aplicarInsetsDelSistema()

        // =====================================
        // COMPONENTES
        // =====================================

        val etCorreo = findViewById<EditText>(R.id.etCorreo)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnIngresar = findViewById<Button>(R.id.btnIngresar)
        val btnCrearCuenta = findViewById<Button>(R.id.btnCrearCuenta)
        val tvMensaje = findViewById<TextView>(R.id.tvMensaje)

        // =====================================
        // BOTÓN INGRESAR
        // =====================================

        btnIngresar.setOnClickListener {

            val correo = etCorreo.text.toString().trim()
            val password = etPassword.text.toString()

            // VALIDACIONES
            if (correo.isBlank()) {
                etCorreo.error = "Ingrese el correo"
                return@setOnClickListener
            }

            if (password.isBlank()) {
                etPassword.error = "Ingrese la contraseña"
                return@setOnClickListener
            }

            val loginRequest = LoginRequest(
                correo = correo,
                password = password
            )

            tvMensaje.text = "Validando usuario..."

            // Evitar varios clics mientras responde la API
            btnIngresar.isEnabled = false

            // CONSUMIR API .NET
            ApiClient.usuarioApi
                .login(loginRequest)
                .enqueue(object : Callback<Usuario> {

                    override fun onResponse(
                        call: Call<Usuario>,
                        response: Response<Usuario>
                    ) {
                        btnIngresar.isEnabled = true

                        val usuario = response.body()

                        // LOGIN CORRECTO
                        if (response.isSuccessful && usuario != null) {

                            tvMensaje.text = ""

                            val intent = Intent(
                                this@LoginActivity,
                                MainActivity::class.java
                            )

                            // Datos del usuario autenticado (vienen de la respuesta del login)
                            intent.putExtra(MainActivity.EXTRA_NOMBRE, usuario.nombre)
                            intent.putExtra(MainActivity.EXTRA_APELLIDO, usuario.apellido)
                            intent.putExtra(MainActivity.EXTRA_CORREO, usuario.correo)

                            startActivity(intent)

                            // Cerrar pantalla de Login
                            finish()

                        } else if (response.isSuccessful) {
                            tvMensaje.text =
                                "No se pudo obtener la información del usuario"
                        } else {
                            // 401 "Correo o contraseña incorrectos." u otro mensaje del API
                            tvMensaje.text = ApiError.mensaje(response)
                        }
                    }

                    override fun onFailure(
                        call: Call<Usuario>,
                        t: Throwable
                    ) {
                        btnIngresar.isEnabled = true

                        tvMensaje.text =
                            "No se pudo conectar con el servidor: ${t.message}"
                    }
                })
        }

        // =====================================
        // BOTÓN CREAR CUENTA
        // =====================================

        btnCrearCuenta.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }
}
