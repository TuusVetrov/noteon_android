package ru.noteon.presentation.ui.home

data class HomeState(
    val isLoggedIn: Boolean?
) {
    companion object {
        val init = HomeState(
            isLoggedIn = null,
        )
    }
}
