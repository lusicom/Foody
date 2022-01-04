package com.example.foody.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow

class NetworkListener : ConnectivityManager.NetworkCallback(){

    private val isNetworkAvailable = MutableStateFlow(false)

    //function checks if device has internet connection and if yes assigned true to isNetworkAvailible variable
    fun checkNetworkAvailability(context: Context): MutableStateFlow<Boolean> {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(this)

        var isConnected = false
        connectivityManager.allNetworks.forEach { network ->
            val networkCapability = connectivityManager.getNetworkCapabilities(network)
            networkCapability?.let {
               // if device has internet connection
                if (it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)){
                    isConnected = true
                    return@forEach
                }
            }
        }

        isNetworkAvailable.value = isConnected

        return isNetworkAvailable
    }

    override fun onAvailable(network: Network) {
        isNetworkAvailable.value = true
    }

    override fun onLost(network: Network) {
        isNetworkAvailable.value = false
    }
}