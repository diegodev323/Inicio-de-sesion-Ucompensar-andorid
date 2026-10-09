package com.example.parcialkotlinapp

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.parcialkotlinapp.api.ApiClient
import com.example.parcialkotlinapp.api.ApiError
import com.example.parcialkotlinapp.models.CrearUsuarioRequest
import com.example.parcialkotlinapp.models.Usuario
import com.example.parcialkotlinapp.util.FechaUtils
import com.example.parcialkotlinapp.util.aplicarInsetsDelSistema
import com.google.android.material.appbar.MaterialToolbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class RegistroActivity : AppCompatActivity() {

    // Fecha elegida en el DatePicker (formato de la API: yyyy-MM-dd)
    private var fechaApi: String = ""
    private var edadCalculada: Int = -1

    // Última fecha elegida (para reabrir el DatePicker donde quedó)
    private var anioSel = 0
    private var mesSel = 0   // 0-11, como Calendar
    private var diaSel = 0

    private val colorError = Color.parseColor("#B3261E")
    private val colorNormal = Color.parseColor("#4A424D")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)
        findViewById<android.view.View>(R.id.rootRegistro).aplicarInsetsDelSistema()

        // FLECHA SUPERIOR: regresa a la pantalla anterior (Login)
        findViewById<MaterialToolbar>(R.id.toolbar)
            .setNavigationOnClickListener { finish() }

        // COMPONENTES DE LA VISTA
        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etApellido = findViewById<EditText>(R.id.etApellido)
        val etCorreo = findViewById<EditText>(R.id.etCorreoRegistro)
        val etPassword = findViewById<EditText>(R.id.etPasswordRegistro)
        val etFechaNacimiento = findViewById<EditText>(R.id.etFechaNacimiento)
        val tvEdad = findViewById<TextView>(R.id.tvEdadCalculada)
        val etUniversidad = findViewById<EditText>(R.id.etUniversidad)
        val spSemestre = findViewById<Spinner>(R.id.spSemestre)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)
        val tvMensaje = findViewById<TextView>(R.id.tvMensajeRegistro)

        // =====================================
        // SEMESTRE: lista con valores 1 a 10
        // (la posición 0 es solo el texto "Seleccione")
        // =====================================
        val opciones = listOf("Seleccione el semestre") + (1..10).map { it.toString() }
        spSemestre.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            opciones
        )

        // =====================================
        // FECHA DE NACIMIENTO: DatePicker
        // =====================================
        etFechaNacimiento.setOnClickListener {
            val ahora = Calendar.getInstance()

            // Primera vez: sugerir hace 18 años
            val anio = if (anioSel != 0) anioSel else ahora.get(Calendar.YEAR) - 18
            val mes = if (anioSel != 0) mesSel else ahora.get(Calendar.MONTH)
            val dia = if (anioSel != 0) diaSel else ahora.get(Calendar.DAY_OF_MONTH)

            val dialogo = DatePickerDialog(
                this,
                { _, a, m, d ->
                    anioSel = a
                    mesSel = m
                    diaSel = d

                    fechaApi = FechaUtils.formatoApi(a, m + 1, d)
                    etFechaNacimiento.setText(FechaUtils.formatoVisible(a, m + 1, d))
                    etFechaNacimiento.error = null

                    // Edad exacta (día, mes y año)
                    edadCalculada = FechaUtils.calcularEdadHoy(a, m + 1, d)
                    tvEdad.text = "Edad: $edadCalculada años"
                },
                anio, mes, dia
            )
            // No se permiten fechas futuras
            dialogo.datePicker.maxDate = System.currentTimeMillis()
            dialogo.show()
        }

        // =====================================
        // BOTÓN REGISTRAR
        // =====================================
        btnRegistrar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val apellido = etApellido.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val password = etPassword.text.toString()
            val universidad = etUniversidad.text.toString().trim()
            val semestre = spSemestre.selectedItemPosition   // 0 = sin elegir, 1..10 = semestre

            // VALIDACIONES
            if (nombre.isBlank()) {
                etNombre.error = "Ingrese el nombre"
                return@setOnClickListener
            }

            if (apellido.isBlank()) {
                etApellido.error = "Ingrese el apellido"
                return@setOnClickListener
            }

            if (correo.isBlank()) {
                etCorreo.error = "Ingrese el correo"
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                etCorreo.error = "Correo no válido"
                return@setOnClickListener
            }

            if (password.length < 6) {
                etPassword.error = "La contraseña debe tener mínimo 6 caracteres"
                return@setOnClickListener
            }

            if (fechaApi.isBlank()) {
                etFechaNacimiento.error = "Seleccione la fecha de nacimiento"
                mostrarMensaje(tvMensaje, "Seleccione la fecha de nacimiento", true)
                return@setOnClickListener
            }

            if (universidad.isBlank()) {
                etUniversidad.error = "Ingrese la universidad"
                return@setOnClickListener
            }

            if (semestre < 1 || semestre > 10) {
                mostrarMensaje(tvMensaje, "Seleccione un semestre entre 1 y 10", true)
                return@setOnClickListener
            }

            // OBJETO QUE SE ENVÍA AL BACKEND (sin rol y sin edad)
            val nuevoUsuario = CrearUsuarioRequest(
                nombre = nombre,
                apellido = apellido,
                correo = correo,
                password = password,
                fechaNacimiento = fechaApi,
                universidad = universidad,
                semestre = semestre
            )

            mostrarMensaje(tvMensaje, "Registrando usuario...", false)
            btnRegistrar.isEnabled = false

            // LLAMADA AL BACKEND CON RETROFIT
            ApiClient.usuarioApi
                .crearUsuario(nuevoUsuario)
                .enqueue(object : Callback<Usuario> {

                    override fun onResponse(
                        call: Call<Usuario>,
                        response: Response<Usuario>
                    ) {
                        btnRegistrar.isEnabled = true

                        if (response.isSuccessful) {
                            tvMensaje.text = ""

                            // Cuadro emergente de registro exitoso
                            AlertDialog.Builder(this@RegistroActivity)
                                .setTitle("Registro exitoso")
                                .setMessage("El usuario fue creado exitosamente. Ya puede iniciar sesión.")
                                .setPositiveButton("ACEPTAR") { _, _ ->
                                    // Regresar al Login para iniciar sesión
                                    finish()
                                }
                                .setCancelable(false)
                                .show()

                        } else {
                            // Se muestra el mensaje que envía la API, p. ej.
                            // HTTP 400 { "mensaje": "Ya existe un usuario con ese correo." }
                            val mensaje = ApiError.mensaje(response)
                            mostrarMensaje(tvMensaje, mensaje, true)

                            if (mensaje.contains("correo", ignoreCase = true)) {
                                etCorreo.error = mensaje
                            }
                        }
                    }

                    override fun onFailure(call: Call<Usuario>, t: Throwable) {
                        btnRegistrar.isEnabled = true
                        mostrarMensaje(
                            tvMensaje,
                            "No se pudo conectar con el servidor: ${t.message}",
                            true
                        )
                    }
                })
        }
    }

    private fun mostrarMensaje(tv: TextView, texto: String, esError: Boolean) {
        tv.text = texto
        tv.setTextColor(if (esError) colorError else colorNormal)
    }
}
