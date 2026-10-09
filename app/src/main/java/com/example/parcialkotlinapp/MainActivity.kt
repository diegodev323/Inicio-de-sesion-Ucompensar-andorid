package com.example.parcialkotlinapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.parcialkotlinapp.util.FechaUtils
import com.example.parcialkotlinapp.util.aplicarInsetsDelSistema
import com.google.android.material.appbar.MaterialToolbar
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_NOMBRE = "nombreUsuario"
        const val EXTRA_APELLIDO = "apellidoUsuario"
        const val EXTRA_CORREO = "correoUsuario"
    }

    private var nombreUsuario: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // El nombre solo puede venir del Login. Si no hay sesión, volver al Login.
        val nombre = intent.getStringExtra(EXTRA_NOMBRE)
        if (nombre.isNullOrBlank()) {
            irAlLogin()
            return
        }
        nombreUsuario = nombre

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        findViewById<android.view.View>(R.id.main).aplicarInsetsDelSistema()

        // =====================================
        // BARRA SUPERIOR: ícono de cerrar sesión
        // =====================================
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.inflateMenu(R.menu.menu_main)
        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_logout) {
                confirmarCierreSesion()
                true
            } else {
                false
            }
        }

        // =====================================
        // BOTÓN EJERCICIO PAR O IMPAR
        // =====================================
        findViewById<Button>(R.id.btnParImpar).setOnClickListener {
            startActivity(Intent(this, ParImparActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        if (nombreUsuario.isNotEmpty()) mostrarSaludo()
    }

    // =====================================
    // SALUDO SEGÚN LA HORA DEL DISPOSITIVO
    // 05:00-11:59 Buenos días | 12:00-17:59 Buenas tardes | 18:00-04:59 Buenas noches
    // =====================================
    private fun mostrarSaludo() {
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        findViewById<TextView>(R.id.tvSaludo).text =
            "${FechaUtils.saludoSegunHora(hora)}, $nombreUsuario"
    }

    // =====================================
    // CIERRE DE SESIÓN CON CONFIRMACIÓN
    // =====================================
    private fun confirmarCierreSesion() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Está seguro de que desea cerrar sesión?")
            .setNegativeButton("CANCELAR") { dialog, _ ->
                // Solo cierra el cuadro; el usuario sigue en la pantalla principal
                dialog.dismiss()
            }
            .setPositiveButton("SALIR") { _, _ ->
                irAlLogin()
            }
            .show()
    }

    /**
     * Regresa al Login borrando todo el historial de pantallas, para que el
     * botón Atrás no permita volver a una pantalla de la sesión anterior.
     */
    private fun irAlLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}
