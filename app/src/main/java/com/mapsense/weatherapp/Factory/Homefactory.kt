package com.mapsense.weatherapp.Factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lotteryapp.utility.interfaces.onApiCall
import com.mapsense.weatherapp.Viewmodel.Homeviewmodel

class Homefactory(private val mApplication: Application, private val apiCall: onApiCall) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return Homeviewmodel(mApplication,apiCall) as T
    }

}