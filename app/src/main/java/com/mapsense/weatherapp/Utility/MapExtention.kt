package com.mapsense.weatherapp.Utility

import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

const val TASK_AWAIT = 120L

fun getAutocomplete(mPlacesClient: PlacesClient, constraint: CharSequence): List<AutocompletePrediction> {
    var list = listOf<AutocompletePrediction>()
    val token = AutocompleteSessionToken.newInstance()
    val request = FindAutocompletePredictionsRequest.builder().setTypeFilter(TypeFilter.CITIES).setSessionToken(token).setQuery(constraint.toString()).build()
    val prediction = mPlacesClient.findAutocompletePredictions(request)
    try {
        Tasks.await(prediction, TASK_AWAIT, TimeUnit.SECONDS)
    } catch (e: ExecutionException) {
        e.printStackTrace()
    } catch (e: InterruptedException) {
        e.printStackTrace()
    } catch (e: TimeoutException) {
        e.printStackTrace()
    }

    if (prediction.isSuccessful) {
        val findAutocompletePredictionsResponse = prediction.result
        findAutocompletePredictionsResponse?.let {
            list = findAutocompletePredictionsResponse.autocompletePredictions
        }
        return list
    }
    return list
}