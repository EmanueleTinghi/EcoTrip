package it.unipi.dii.masss_project

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.AsyncTask
import java.util.Locale

class GeocoderTask(private val context: Context, private val listener: OnGeocoderCompletedListener) :
    AsyncTask<Location, Void, String>() {

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg locations: Location): String? {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(locations[0].latitude, locations[0].longitude, 1)
        return addresses?.get(0)?.locality
    }

    @Deprecated("Deprecated in Java")
    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        listener.onGeocoderCompleted(result)
    }

    interface OnGeocoderCompletedListener {
        fun onGeocoderCompleted(cityName: String?)
    }
}
