package com.denchic45.kts.data.service

import android.content.Context
import android.net.ConnectivityManager

class AndroidNetworkService(private val context: Context) : NetworkService {
    override val isNetworkAvailable: Boolean
        get() {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
}