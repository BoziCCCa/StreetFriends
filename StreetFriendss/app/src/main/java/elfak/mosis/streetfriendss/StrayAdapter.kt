package elfak.mosis.streetfriendss

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LiveData
import com.bumptech.glide.Glide
import elfak.mosis.streetfriendss.classes.StrayAnimal
import java.text.SimpleDateFormat
import java.util.Locale

class StrayAdapter(private val context: Context, private val strayListLiveData: LiveData<List<StrayAnimal>>) : BaseAdapter() {

    private var strayList: List<StrayAnimal> = emptyList()

    init {
        // Observe the LiveData and update the local list when data changes
        strayListLiveData.observeForever { strayList ->
            this.strayList = strayList
            notifyDataSetChanged()
        }
    }

    override fun getCount(): Int {
        return strayList.size
    }

    override fun getItem(p0: Int): Any {
        return strayList[p0]
    }
    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var myView = convertView
        val holder: ViewHolder

        if (myView == null) {
            myView = LayoutInflater.from(context).inflate(R.layout.custom_stray_listview, parent, false)
            holder = ViewHolder(myView)
            myView.tag = holder
        } else {
            holder = myView.tag as ViewHolder
        }

        val stray = strayList[position]
        holder.strayNameTextView.text = stray.name
        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val formatted = formatter.format(stray.date)
        holder.strayDateTextView.text = formatted
        holder.strayTypeTextView.text = stray.type
        holder.strayDescriptionTextView.text = stray.description

        Glide.with(context).load(stray.photo).into(holder.strayImageView)
        holder.latitudeTextView.text = "Latitude: ${stray.lat}"
        holder.longitudeTextView.text = "Longitude: ${stray.lon}"

        return myView!!
    }
    private class ViewHolder(view: View) {
        val strayNameTextView: TextView = view.findViewById(R.id.strayNameTextView)
        val strayDateTextView: TextView = view.findViewById(R.id.strayDateTextView)
        val strayTypeTextView: TextView = view.findViewById(R.id.strayTypeTextView)
        val strayDescriptionTextView: TextView = view.findViewById(R.id.strayDescriptionTextView)
        val strayImageView: ImageView = view.findViewById(R.id.strayImageView)
        val latitudeTextView: TextView = view.findViewById(R.id.latitudeTextView)
        val longitudeTextView: TextView = view.findViewById(R.id.longitudeTextView)
    }

    fun updateStrayList(newList: List<StrayAnimal>) {
        strayList = newList
        notifyDataSetChanged()
    }
}