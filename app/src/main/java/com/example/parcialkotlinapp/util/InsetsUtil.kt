package com.example.parcialkotlinapp.util

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Con edge-to-edge (obligatorio en Android 15+) el contenido queda detrás de la
 * barra de estado y de navegación. Esta función suma esos márgenes al padding
 * que la vista ya tenía, y también tiene en cuenta el teclado.
 */
fun View.aplicarInsetsDelSistema() {
    val izq = paddingLeft
    val arr = paddingTop
    val der = paddingRight
    val aba = paddingBottom

    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val b = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or
                    WindowInsetsCompat.Type.displayCutout() or
                    WindowInsetsCompat.Type.ime()
        )
        v.setPadding(izq + b.left, arr + b.top, der + b.right, aba + b.bottom)
        insets
    }
}
