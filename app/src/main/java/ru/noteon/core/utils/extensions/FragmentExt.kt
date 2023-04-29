package ru.noteon.core.utils.extensions

import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import ru.noteon.R

fun Fragment.snackBar(message: String) {
    view?.let {
        Snackbar.make(it, message, Snackbar.LENGTH_SHORT).apply {
            setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.light_error))
            animationMode = Snackbar.ANIMATION_MODE_FADE
            show()
        }
    }
}