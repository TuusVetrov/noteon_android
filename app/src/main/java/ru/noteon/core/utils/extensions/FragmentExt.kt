package ru.noteon.core.utils.extensions

import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import ru.noteon.R

fun Fragment.snackBar(message: String) {
    view?.let {
        Snackbar.make(it, message, Snackbar.LENGTH_SHORT).apply {
            setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.noteon_error))
            animationMode = Snackbar.ANIMATION_MODE_FADE
            show()
        }
    }
}

fun View.showKeyboard() {
    val inputMethodManager = context.getSystemService<InputMethodManager>()
    inputMethodManager?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun View.hideKeyboard() {
    val inputMethodManager = context.getSystemService<InputMethodManager>()
    inputMethodManager?.hideSoftInputFromWindow(windowToken, 0)
}