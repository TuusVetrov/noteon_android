package ru.noteon.core.utils.extensions

import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.ViewModel
import ru.noteon.R

inline fun <reified T : ViewModel> Fragment.hiltMainNavGraphViewModels() =
    hiltNavGraphViewModels<T>(R.id.nav_graph)
