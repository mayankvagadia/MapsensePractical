package com.mapsense.weatherapp.Utility

import android.content.Context
import android.net.ConnectivityManager
import com.mapsense.weatherapp.R

object Utils {
    fun isNetworkConnected(activity: Context): Boolean {
        val cm = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null
    }

    fun getNointernet(activity: Context): String {
        return activity.resources.getString(R.string.noInternet)
    }
}