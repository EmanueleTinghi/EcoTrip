package it.unipi.dii.masss_project

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class GPSHandler(context: Context) {

    // initialize application context
    private val applicationContext = context

    // initialize location manager
    private var locationManager : LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private lateinit var locationListener :LocationListener

    var progress = "Start"

    private var startPoint: Location = Location("Start point")
    private var endPoint: Location = Location("End point")

    lateinit var startCity: String

    val distances = mutableListOf<Double>()

    init {
        // initialize location listener
        initializeLocationListener()
    }

    private fun initializeLocationListener() {

        locationListener = object : LocationListener {
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

            override fun onLocationChanged(location: Location) {

                // get coordinates
                val latitude = location.latitude
                val longitude = location.longitude

                when (progress) {
                    "Start" -> {
                        // start point
                        startPoint.latitude = latitude
                        startPoint.longitude = longitude

                        // end point
                        endPoint.latitude = latitude
                        endPoint.longitude = longitude

                        // get start city from gps coordinates
                        val geocoderTask = GeocoderTask(applicationContext, object : GeocoderTask.OnGeocoderCompletedListener {
                            override fun onGeocoderCompleted(cityName: String?) {
                                if (cityName != null) {
                                    startCity = cityName
                                    println("START CITY: $startCity")
                                }
                            }
                        })
                        geocoderTask.execute(startPoint)

                        println("START LOCATION: latitude ${startPoint.latitude}, longitude ${startPoint.longitude}")
                        progress ="Intermediate"

                    }
                    "Stop" -> {

                        // stop point
                        endPoint.latitude = latitude
                        endPoint.longitude = longitude
                        println("STOP LOCATION: latitude ${endPoint.latitude}, longitude ${endPoint.longitude}")

                        // calculate distance between intermediate and end point
                        val distance = (startPoint.distanceTo(endPoint) / 1000.0)
                        println("DISTANCE: $distance km")
                        distances.add(distance)

                        // Stop receiving location updates
                        locationManager.removeUpdates(locationListener)

                    }
                    else -> {
                        // intermediate point
                        endPoint.latitude = latitude
                        endPoint.longitude = longitude
                        println("INTERMEDIATE LOCATION: latitude ${endPoint.latitude}, longitude ${endPoint.longitude}")

                        // calculate distance between start and intermediate point
                        val distance = (startPoint.distanceTo(endPoint) / 1000.0)
                        println("DISTANCE: $distance km")
                        distances.add(distance)

                        // set new start point
                        startPoint.latitude = latitude
                        startPoint.longitude = longitude
                    }
                }
            }
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}

        }
    }

    fun checkLocationPermission() {
        // Check if the user has granted location permissions at runtime
        if (ContextCompat.checkSelfPermission(applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission
            ActivityCompat.requestPermissions(
                applicationContext as Activity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION), 0)
        }
    }

    fun startReceivingUpdates(){
        checkLocationPermission()
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5.0f, locationListener)
    }

    fun stopReceivingUpdates(){
        locationManager.removeUpdates(locationListener)
    }
}