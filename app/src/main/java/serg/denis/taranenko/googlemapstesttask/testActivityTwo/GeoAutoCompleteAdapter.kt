package serg.denis.taranenko.googlemapstesttask.testActivityTwo

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import serg.denis.taranenko.googlemapstesttask.R
import serg.denis.taranenko.googlemapstesttask.getLocationsString
import java.lang.ref.WeakReference


class GeoAutoCompleteAdapter: BaseAdapter(), Filterable {

    companion object {
        val MAX_RESULTS = 10
    }

    private lateinit var mContext:WeakReference<Context>
    private var resultList:List<GeoSearchResult> = ArrayList()

    fun GeoAutoCompleteAdapter(context: Context){
        mContext = WeakReference(context)
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertViewCopy = convertView

        if (convertViewCopy == null && mContext.get() != null){
            val inflater = mContext.get()!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertViewCopy = inflater.inflate(R.layout.list_item, parent, false)
        }

        convertViewCopy?.findViewById<TextView>(R.id.list_item)?.text =
                getItem(position).getAddress()

        return convertViewCopy!!
    }

    override fun getItem(position: Int): GeoSearchResult {
        return resultList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return resultList.size
    }

    override fun getFilter(): Filter {
        Log.d("GeoAutoCompleteAdapter", "getFilter()")
        return object: Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()

                if (constraint != null && mContext.get() != null){
                    val locations = getLocationsString(mContext.get()!!, constraint.toString())

                    filterResults.values = locations
                    filterResults.count = locations.size
                }

                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.count > 0){
                    resultList = results.values as List<GeoSearchResult>
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }

        }
    }
}