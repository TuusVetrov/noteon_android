package ru.noteon.core.connectivity

import android.net.ConnectivityManager
import kotlinx.coroutines.flow.Flow
import ru.noteon.core.utils.currentConnectivityState
import ru.noteon.core.utils.observeConnectivityAsFlow

class ConnectivityObserver(
    private val connectivityManager: ConnectivityManager
) {
    val connectionState: Flow<ConnectionState>
        get() = connectivityManager.observeConnectivityAsFlow()

    val currentConnectionState: ConnectionState
        get() = connectivityManager.currentConnectivityState
}