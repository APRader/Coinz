package com.example.s1611382.ilp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.DrawableWrapper
import android.graphics.drawable.Icon
import android.location.Location
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.example.s1611382.ilp.MainActivity.DownloadCompleteRunner.result
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core. permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), PermissionsListener, LocationEngineListener, OnMapReadyCallback {

    private val tag = "MainActivity"
    private var mapView: MapView? = null
    private var map: MapboxMap? = null
    private var downloadDate = "" // Format: YYYY/MM/DD
    private var lastJson = ""
    private val preferencesFile = "MyPrefsFile" // for storing preferences
    // each item in the list contains a marker and the details of the corresponding coin
    private var markerPairs: MutableList<Pair<List<String>, Marker?>> = mutableListOf()

    var coins: MutableList<String> = mutableListOf()

    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var permissionManager: PermissionsManager
    //stores current location at all times
    private lateinit var originLocation: Location
    //contains GeoJson features
    private lateinit var coinCollection: FeatureCollection
    private lateinit var features: List<Feature>

    //gives user location
    private lateinit var locationEngine: LocationEngine
    //for UI: icon representing user location
    private lateinit var locationLayerPlugin: LocationLayerPlugin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //need toolbar for app nav drawer button
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu)
        }

        Mapbox.getInstance(this, getString(R.string.access_token))
        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        // this makes map variable usable in the rest of the class
        mapView?.getMapAsync(this)

        mDrawerLayout = findViewById(R.id.drawer_layout)

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            // set item as selected to persist highlight
            menuItem.isChecked = true
            // close drawer when item is tapped
            mDrawerLayout.closeDrawers()

            // Add code here to update the UI based on the item selected
            // For example, swap UI fragments here
            if (menuItem.itemId == R.id.nav_wallet) {
                openWallet()
            }

            true
        }

        val fab: View = findViewById(R.id.fab)
        fab.setOnClickListener {
            if (::originLocation.isInitialized) {
                setCameraPosition(originLocation)
            } else {
                Log.d(tag, "[fab] originLocation not initialized")
            }
        }

    }

    override fun onMapReady(mapboxMap: MapboxMap?) {
        if (mapboxMap == null) {
            Log.d(tag, "[onMapReady] mapBoxMap is null")
        } else {
            map = mapboxMap
            map?.uiSettings?.isCompassEnabled = true
            map?.uiSettings?.isZoomControlsEnabled = true

            // Make location information available
            enableLocation()

            // Get the GeoJSON marker coordinates from the web
            downloadMap()
        }
    }

    //get user's permission for location
    private fun enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            initializeLocationEngine()
            initializeLocationLayer()
        } else {
            Log.d(tag, "Permissions are not granted")
            permissionManager = PermissionsManager(this)
            permissionManager.requestLocationPermissions(this)
        }
    }

    @SuppressLint("MissingPermission")
    private fun initializeLocationEngine() {
        locationEngine = LocationEngineProvider(this).obtainBestLocationEngineAvailable()
        locationEngine.addLocationEngineListener(this)
        locationEngine.apply {
            interval = 5000
            fastestInterval = 1000
            priority = LocationEnginePriority.HIGH_ACCURACY
            activate()
        }

        val lastLocation = locationEngine.lastLocation
        if (lastLocation != null) {
            originLocation = lastLocation
            setCameraPosition(lastLocation)
        } //else { locationEngine.addLocationEngineListener(this) }
    }

    private fun initializeLocationLayer() {
        if (mapView == null) { Log.d(tag, "mapView is null") }
        else {
            if (map == null) { Log.d(tag, "map is null") }
            else {
                locationLayerPlugin = LocationLayerPlugin(mapView!!, map!!, locationEngine)
                //for camera tracking location
                locationLayerPlugin.apply {
                    setLocationLayerEnabled(true)
                    cameraMode = CameraMode.TRACKING
                    renderMode = RenderMode.NORMAL
                }
            }
        }
    }

    private fun setCameraPosition(location: Location) {
        if (location == null) {
            Log.d(tag, "[setCameraPosition] location is null")
        } else {
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(location.latitude, location.longitude), 15.5))
        }
    }

    private fun openWallet() {
        val intent = Intent(this, Wallet::class.java)
        startActivity(intent)
    }

    private fun drawCoinLocations(JsonFile : String?) {

        coinCollection = FeatureCollection.fromJson(JsonFile.toString())

        features = coinCollection.features() as List<Feature>

        for (f: Feature in features) {
            if (f.geometry() is Point) {
                val coordinates = (f.geometry() as Point).coordinates()

                val coinValue = f.properties()?.get("value").toString().removeSurrounding("\"")
                val coinColour = f.properties()?.get("marker-color").toString()
                        .removeSurrounding("\"").removePrefix("#")
                val coinSymbol = f.properties()?.get("marker-symbol").toString()
                        .removeSurrounding("\"")
                val coinCurrency = f.properties()?.get("currency").toString()
                        .removeSurrounding("\"")
                val coinId = f.properties()?.get("id").toString()
                        .removeSurrounding("\"")

                val pin = "pin_$coinSymbol" + "_$coinColour"
                val resID = resources.getIdentifier(pin, "drawable", packageName)
                val bm = BitmapFactory.decodeResource(resources, resID)
                val coinIcon = IconFactory.getInstance(this).fromBitmap(bm)

                val markerOpt  = MarkerOptions().position(LatLng(coordinates[1], coordinates[0]))
                        .title("$coinCurrency $coinValue").icon(coinIcon)
                // We need the marker, not the marker option saved in the list, so we can remove it later
                val marker: Marker? = map?.addMarker(markerOpt)
                val coinDetails = listOf(coinId, coinCurrency, coinValue)
                val pair = Pair(coinDetails, marker)
                markerPairs.add(pair)
            }

        }
    }

    private fun downloadMap()   {
        // get current date
        // update download date as a string
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.UK)
        val today = sdf.format(Date())

        //get map data if download date is old
        if (today != downloadDate) {
            val task = DownloadFileTask(DownloadCompleteRunner)
            lastJson = task.execute("http://homepages.inf.ed.ac.uk/stg/coinz/$today/coinzmap.geojson").get()
        }
        // check if Json contains valid coin data (i.e. download was successful)
        if (lastJson.startsWith("\n{\n")){
            drawCoinLocations(lastJson)
            //only update download date if download was successful and coins are drawn
            downloadDate = today
        } else {
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle("No network connection")
            builder.setMessage("Unable to load content. " +
                    "Check your network connection and relaunch your app to try again")
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

    private fun nearCoin(location: Location?) {
        if (::features.isInitialized) {
            //check if player is near a coin
            for (p: Pair<List<String>, Marker?> in markerPairs) {
                val coinLocation = Location("")
                val marker = p.second
                if (marker != null) {
                    coinLocation.latitude = marker.position.latitude
                    coinLocation.longitude = marker.position.longitude
                    //player is within 25 metres of the coin
                    if (location!!.distanceTo(coinLocation) <= 25) {
                        val coinDetails = p.first
                        val coinId = coinDetails[0]
                        // coin id is added to your coin collection
                        coins.add(coinId)
                        val builder = AlertDialog.Builder(this@MainActivity)
                        builder.setTitle("Coin collected")
                        builder.setMessage("You collected coin $coinId!")
                        val dialog: AlertDialog = builder.create()
                        dialog.show()
                        // coin is deleted from map
                        map?.removeMarker(marker)
                    }
                }
            }

        } else {
            Log.d(tag, "features is empty")
        }
    }

    interface DownloadCompleteListener {
        fun downloadComplete(result: String)
    }

    object DownloadCompleteRunner : DownloadCompleteListener {
        var result : String? = null
        override fun downloadComplete(result: String) {
            this.result = result

        }
    }

    class DownloadFileTask(private val caller : DownloadCompleteListener):
            AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg urls: String): String = try {
            loadFileFromNetwork(urls[0])
        } catch (e: IOException) {
            "Unable to load content. check your network connection"
        }

        private fun loadFileFromNetwork(urlString: String): String {
            val stream: InputStream = downloadUrl(urlString)
            // Read input from stream, build result as a string
            result = stream.bufferedReader().use { it.readText() }
            return result.toString()
        }


        //Given a string representation of a URL, sets up a connection and gets an input stream.
        @Throws(IOException::class)
        private fun downloadUrl(urlString: String): InputStream {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.readTimeout = 10000 // milliseconds
            conn.connectTimeout = 15000 // milliseconds
            conn.requestMethod = "GET"
            conn.doInput = true
            conn.connect() // Starts the query
            return conn.inputStream
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            caller.downloadComplete(result)
        }
    }


    //open the drawer when nav button is tapped
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                mDrawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else ->super.onOptionsItemSelected(item)
        }
    }

    //-- from PermissionsListener --//
    //when user denies permission
    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        /*seems to trigger when location previously not granted when app opens
        but doesn't trigger when first denied.
        Hence warning message built into onPermissionResult function*/
    }
    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocation()
        } else {
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle("Location permission not granted")
            builder.setMessage("You denied permission to use location. " +
                    "Without that information you cannot collect new coins. " +
                    "Please accept access to location when you reopen the app " +
                    "so you can enjoy Coinz to the fullest extent!")
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //-- from LocationEngineListener --//
    override fun onLocationChanged(location: Location?) {
        if (location == null) {
            Log.d(tag, "[onLocationChanged] location is null")
        } else {
            originLocation = location
            //setCameraPosition(originLocation)
            nearCoin(location)
        }
    }
    @SuppressLint("MissingPermission")
    override fun onConnected() {
        Log.d(tag, "[onConnected] requesting location updates")
        locationEngine.requestLocationUpdates()
    }


    // lifecycle methods for MapBox override standard funs
    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
        mapView?.onStart()

        // Restore preferences
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)

        // use "" as the default value (this might be the first time the app is run)
        downloadDate = settings.getString("lastDownloadDate", "")
        lastJson = settings.getString("lastJson", "")

        Log.d(tag, "[onStart] Recalled lastDownloadDate is '$downloadDate'")
        Log.d(tag, "[onStart] Recalled lastJson is '$lastJson'")
    }
    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }
    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }
    override fun onStop() {
        super.onStop()
        locationEngine.removeLocationUpdates()
        locationLayerPlugin.onStop()
        mapView?.onStop()

        Log.d(tag, "[onStop] Storing lastDownloadDate of $downloadDate")
        Log.d(tag, "[onStop] Storing lastJson of $lastJson")

        // All objects are from android.context.Context
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)

        // We need an Editor object o make preference changes
        val editor = settings.edit()
        editor.putString("lastDownloadDate", downloadDate)
        editor.putString("lastJson", lastJson)
        // Apply the edits!
        editor.apply()
    }
    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
        locationEngine.deactivate()
    }
    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (outState != null) {
            mapView?.onSaveInstanceState(outState)
        }
    }
    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }
}

