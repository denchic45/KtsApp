//package com.denchic45.kts.data
//
//import android.content.Context
//import android.net.ConnectivityManager
//import javax.inject.Inject
//
//class NetworkService @Inject constructor(private val context: Context) {
//    val isNetworkAvailable: Boolean
//        get() {
//            val connectivityManager =
//                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//            val activeNetworkInfo = connectivityManager.activeNetworkInfo
//            return activeNetworkInfo != null && activeNetworkInfo.isConnected
//        }
//
//    fun listenNetworkAvailable(): ConnectionLiveData {
//        return ConnectionLiveData(context)
//    }
//}