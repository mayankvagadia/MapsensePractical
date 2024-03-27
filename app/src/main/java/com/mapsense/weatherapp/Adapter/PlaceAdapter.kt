package com.mapsense.weatherapp.Adapter

import android.content.Context
import android.graphics.Typeface
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.google.android.libraries.places.api.net.PlacesClient
import com.mapsense.weatherapp.Model.PlaceModel
import com.mapsense.weatherapp.R
import com.mapsense.weatherapp.Utility.getAutocomplete

class PlaceAdapter(context: Context, val resource: Int, val mPlacesClient: PlacesClient) : ArrayAdapter<PlaceModel>(context, resource),
    Filterable {

    private var resultList = arrayListOf<PlaceModel>()

    override fun getCount(): Int {
        return when {
            resultList.isEmpty() -> 0
            else -> resultList.size
        }
    }

    override fun getItem(position: Int): PlaceModel? {
        return when {
            resultList.isEmpty() -> null
            else -> resultList[position]
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val viewHolder: ViewHolder
        if (view == null) {
            viewHolder = ViewHolder()
            view = LayoutInflater.from(context).inflate(resource, parent, false)
            viewHolder.description = view.findViewById(R.id.searchFullText) as TextView
            view.tag = viewHolder
        } else {
            viewHolder = view.tag as ViewHolder
        }
        bindView(viewHolder, resultList, position)
        return view!!
    }

    private fun bindView(viewHolder: ViewHolder, place: ArrayList<PlaceModel>, position: Int) {
        if (!place.isNullOrEmpty()) {
            viewHolder.description?.text = place[position].fullText
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint != null) {
                    resultList.clear()
                    val address = getAutocomplete(mPlacesClient, constraint.toString())
                    address.let {
                        for (i in address.indices) {
                            val item = address[i]
                            resultList.add(PlaceModel(item.placeId, item.getFullText(
                                StyleSpan(
                                    Typeface.BOLD)
                            ).toString()))
                        }
                    }
                    filterResults.values = resultList
                    filterResults.count = resultList.size
                }
                return filterResults
            }
        }
    }

    internal class ViewHolder {
        var description: TextView? = null
    }
}