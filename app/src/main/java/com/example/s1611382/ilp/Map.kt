package com.example.s1611382.ilp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.os.CountDownTimer
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.s1611382.ilp.Map.DownloadCompleteRunner.result
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class Map : BaseActivity(), PermissionsListener, LocationEngineListener, OnMapReadyCallback {

    private var mapView: MapView? = null
    private var map: MapboxMap? = null
    //gives user location
    private lateinit var locationEngine: LocationEngine
    //for UI: icon representing user location
    private lateinit var locationLayerPlugin: LocationLayerPlugin
    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var permissionManager: PermissionsManager

    // shared preference variables
    private var downloadDate: String? = "" // Format: YYYY/MM/DD
    private var lastJson: String? = ""

    // each item in the list contains a coin and its corresponding marker
    private var markerPairs: MutableList<Pair<Coin, Marker?>> = mutableListOf()
    // each item in the list contains a coin with its different properties
    private var coinWallet : ArrayList<Coin> = arrayListOf()
    private var coinBank : ArrayList<Coin> = arrayListOf()
    private var gold: Double? = 0.0
    private var depositCounter: Int? = 0
    private var counterDate: String? = ""
    // different from wallet: it stores coins which you collected (not traded)
    private var collectedCoins : ArrayList<String> = arrayListOf()
    // Json features of the downloaded file
    private lateinit var features: List<Feature>
    // exchange rates of the day
    private var rates: HashMap<String, Float> = hashMapOf()
    // stores current location at all times
    private lateinit var originLocation: Location

    // for timer
    private var timerStarted = false
    private var timerRunning = false
    private lateinit var countDownTimer: CountDownTimer

    private var firestore: FirebaseFirestore? = null
    private var firebaseUser: FirebaseUser? = null
    private var firebaseEmail: String = ""

    private lateinit var timerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map)
        setToolbar()

        Mapbox.getInstance(this, getString(R.string.access_token))
        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        // this makes map variable usable in the rest of the class
        mapView?.getMapAsync(this)

        firestore = firestoreSetup()
        firebaseUser = FirebaseAuth.getInstance().currentUser
        firebaseEmail = firebaseUser?.email.toString()

        mDrawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            // close drawer when item is tapped
            mDrawerLayout.closeDrawers()

            if (menuItem.itemId == R.id.nav_wallet) { openWallet() }
            if (menuItem.itemId == R.id.nav_bank) { openBank() }
            if (menuItem.itemId == R.id.nav_trading) { openTrading() }
            if (menuItem.itemId == R.id.nav_vending) { openVendingMachine() }
            if (menuItem.itemId == R.id.nav_logout) { logout() }

            false
        }

        timerButton = findViewById(R.id.timer_button_id)
        timerButton.setOnClickListener { startTimer() }

        // centre on current location fab
        val fab: View = findViewById(R.id.fab)
        fab.setOnClickListener {
            if (::originLocation.isInitialized) {
                setCameraPosition(originLocation)
            } else {
                Timber.d("originLocation not initialized")
            }
        }

    }

    /**
     * enables location and starts downloading map
     */
    override fun onMapReady(mapboxMap: MapboxMap?) {
        if (mapboxMap == null) {
            Timber.d("mapBoxMap is null")
        } else {
            map = mapboxMap
            map?.uiSettings?.isCompassEnabled = true
            map?.uiSettings?.isZoomControlsEnabled = true

            enableLocation()

            // Get the GeoJSON marker coordinates from the web
            downloadMap()
        }
    }

    /**
     * get user's permission for location
     */
    private fun enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            initializeLocationEngine()
            initializeLocationLayer()
        } else {
            Timber.d("Permissions are not granted")
            permissionManager = PermissionsManager(this)
            permissionManager.requestLocationPermissions(this)
        }
    }


    /**
     * functions for Mapbox to get user's location
     */
    @SuppressLint("MissingPermission")
    private fun initializeLocationEngine() {
        locationEngine = LocationEngineProvider(this).obtainBestLocationEngineAvailable()
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
        } else { locationEngine.addLocationEngineListener(this) }
    }
    private fun initializeLocationLayer() {
        if (mapView == null) { Timber.d("mapView is null") }
        else {
            if (map == null) { Timber.d("map is null") }
            else {
                locationLayerPlugin = LocationLayerPlugin(mapView!!, map!!, locationEngine)
                //for camera tracking location
                locationLayerPlugin.apply {
                    isLocationLayerEnabled = true
                    cameraMode = CameraMode.TRACKING
                    renderMode = RenderMode.NORMAL
                }
            }
        }
    }
    private fun setCameraPosition(location: Location) {
        // location is never null according to AndroidStudio
        //if (location == null) {
          //  Timber.d("location is null")
       // } else {
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(location.latitude, location.longitude), 15.5))
        //}
    }

    private fun openWallet() {
        val walletIntent = Intent(this, Wallet::class.java)
        walletIntent.putExtra(COIN_WALLET, coinWallet)
        startActivity(walletIntent)
    }
    private fun openBank() {
        val bankIntent = Intent(this, Bank::class.java)
        bankIntent.putExtra(RATES, rates)
        bankIntent.putExtra(COIN_WALLET, coinWallet)
        startActivity(bankIntent)
    }
    private fun openTrading() {
        val tradingIntent = Intent(this, Trading::class.java)
        tradingIntent.putExtra(COIN_WALLET, coinWallet)
        startActivity(tradingIntent)
    }
    private fun openVendingMachine() {
        val vendingIntent = Intent(this, VendingMachine::class.java)
        startActivity(vendingIntent)
    }

    /**
     * uses a JSON file to draw the markers
     * only draws coins that are not already collected
     */
    private fun drawCoinLocations(JsonFile : String?) {

        val coinCollection = FeatureCollection.fromJson(JsonFile.toString())

        features = coinCollection.features() as List<Feature>

        val reader = JSONObject(lastJson)
        val sys = reader.getJSONObject("rates")
        val shil = sys.getString("SHIL").toFloat()
        val dolr = sys.getString("DOLR").toFloat()
        val quid = sys.getString("QUID").toFloat()
        val peny = sys.getString("PENY").toFloat()
        rates.apply {
            put("SHIL", shil)
            put("DOLR", dolr)
            put("QUID", quid)
            put("PENY", peny)
        }

        for (f: Feature in features) {
            if (f.geometry() is Point) {
                val coordinates = (f.geometry() as Point).coordinates()

                // extracting relevant data from the Json
                val coinValue = f.properties()?.get("value").toString()
                        .removeSurrounding("\"")
                val coinColour = f.properties()?.get("marker-color").toString()
                        .removeSurrounding("\"").removePrefix("#")
                val coinSymbol = f.properties()?.get("marker-symbol").toString()
                        .removeSurrounding("\"")
                val coinCurrency = f.properties()?.get("currency").toString()
                        .removeSurrounding("\"")
                val coinId = f.properties()?.get("id").toString()
                        .removeSurrounding("\"")
                val coin = Coin(id = coinId, value = coinValue.toFloat(), currency = coinCurrency)
                // don't draw coin if it was already collected
                if (!collectedCoins.contains(coin.id)) {
                    // getting the Icon for the specific coin using BitmapFactory
                    val pin = "pin_$coinSymbol" + "_$coinColour"
                    val resID = resources.getIdentifier(pin, "drawable", packageName)
                    val bm = BitmapFactory.decodeResource(resources, resID)
                    val coinIcon = IconFactory.getInstance(this).fromBitmap(bm)

                    val markerOpt = MarkerOptions().position(LatLng(coordinates[1], coordinates[0]))
                            .title("$coinCurrency $coinValue").icon(coinIcon)

                    // We need the marker, not the marker option saved in the list, so we can remove it later
                    val marker: Marker? = map?.addMarker(markerOpt)
                    val pair = Pair(coin, marker)
                    markerPairs.add(pair)
                }

            }

        }
    }

    /**
     * downloads JSON map from the website
     * only calls drawCoinLocations if the Json file could be downloaded and is valid
     * otherwise it shows message telling user to check their network connection
     */
    private fun downloadMap()   {
        // get current date
        // update download date as a string
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.UK)
        val today = sdf.format(Date())
        // get map data if download date is old
        // also reset collectedCoins list, as user couldn't have collected any coins today
        if (today != downloadDate || !lastJson!!.startsWith("\n{\n")) {
            collectedCoins = arrayListOf()
            val task = DownloadFileTask(DownloadCompleteRunner)
            lastJson = task.execute(String.format(getString(R.string.json_site), today)).get()
        }

        // check if Json contains valid coin data (i.e. download was successful)
        if (lastJson!!.startsWith("\n{\n")){
            drawCoinLocations(lastJson)
            //only update download date if download was successful and coins are drawn
            downloadDate = today
        } else {
            val builder = AlertDialog.Builder(this@Map)
            builder.setTitle(getString(R.string.no_connection_title))
            builder.setMessage(getString(R.string.no_connection_message))
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

    /**
     * automatically collects coins if player is within 25 metres of it
     * shows message which coin was collected
     */
    private fun nearCoin(location: Location?) {
        if (::features.isInitialized) {
            // need to use iterator, to be able to remove pair in loop
            val it = markerPairs.iterator()
            while (it.hasNext()) {
                val p = it.next()
                val coinLocation = Location("")
                val marker = p.second
                if (marker != null) {
                    coinLocation.latitude = marker.position.latitude
                    coinLocation.longitude = marker.position.longitude
                    //player is within 25 metres of the coin
                    if (location!!.distanceTo(coinLocation) <= 25) {
                        val coin = p.first
                        coinWallet.add(coin)
                        collectedCoins.add(coin.id)

                        val builder = AlertDialog.Builder(this@Map)
                        builder.setTitle(getString(R.string.coin_collected))
                        builder.setMessage(getString(R.string.you_collected) + " $coin!")
                        val dialog: AlertDialog = builder.create()
                        dialog.show()

                        // coin is deleted from map and markerPairs
                        map?.removeMarker(marker)
                        it.remove()
                    }
                }
            }

        } else {
            Timber.d("features is empty")
        }
    }

    /**
     * starts a countdown in the upper right corner
     * informing player for how much longer they can collect coins
     */
    private fun startTimer() {
        timerButton.visibility = View.GONE
        timerStarted = true
        timerRunning = true
        val timerView: TextView = findViewById(R.id.timer_id)

        countDownTimer = object: CountDownTimer(TIMER, INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                val timeLeft = millisUntilFinished/1000
                // format as m:ss
                timerView.text = String.format("%d:%02d", timeLeft/60, timeLeft%60)
            }

            override fun onFinish() {
                timerView.text = ""
                // timer is not running, but has started today, so timerStarted stays true
                timerRunning = false
            }
        }

        countDownTimer.start()
    }

    /**
     * logs out user
     * user is put onto login screen
     */
    private fun logout() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener {
                    val loginIntent = Intent(this, Login::class.java)
                    startActivity(loginIntent)
                    finish()
                }
    }

    /**
     * task for downloading Json data asynchronously
     */
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

    /**
     * only map has toolbar with app drawer button
     * therefore we override the method that creates toolbar with up navigation
     */
    override fun setToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu)
        }
    }

    /**
     * opens the drawer when nav button is tapped
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                mDrawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else ->super.onOptionsItemSelected(item)
        }
    }

    /**
     * from PermissionsListener
     * when user denies or accepts permission
     */
    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        /*seems to trigger when location previously not granted when app opens
        but doesn't trigger when first denied.
        Hence warning message built into onPermissionResult function*/
    }
    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocation()
        } else {
            val builder = AlertDialog.Builder(this@Map)
            builder.setTitle(getString(R.string.no_location_permission_title))
            builder.setMessage(getString(R.string.no_location_permission_message))
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * from LocationEngineListener
     * sets originLocation to new location to be used by other methods
     * if timer is running, calls nearCoin
     */
    override fun onLocationChanged(location: Location?) {
        if (location == null) {
            Timber.d("[onLocationChanged] location is null")
        } else {
            originLocation = location
            if (timerRunning) {
                nearCoin(location)
            }
        }
    }
    @SuppressLint("MissingPermission")
    override fun onConnected() {
        Timber.d("[onConnected] requesting location updates")
        locationEngine.requestLocationUpdates()
    }

    /**
     * gets values from shared preferences
     */
    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
        mapView?.onStart()
        // in order for location to work, we start the location engine again
        if (::locationLayerPlugin.isInitialized) {
            locationLayerPlugin.onStart()
            locationEngine.requestLocationUpdates()
        } else {
            Timber.d("locationLayerPlugin not initialized")
        }
        // Restore preferences
        val prefSettings = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)

        // use "" as the default value (this might be the first time the app is run)
        downloadDate = prefSettings.getString(DOWNLOAD_KEY, "")
        lastJson = prefSettings.getString(JSON_KEY, "")
        gold = prefSettings.getString(GOLD_KEY, "")?.toDoubleOrNull()
        if (gold == null) {
            gold = 0.0
        }
        depositCounter = prefSettings.getString(COUNTER_KEY, "0")?.toIntOrNull()
        if (depositCounter == null) {
            depositCounter = 0
        }

        counterDate = prefSettings.getString(COUNTER_DATE_KEY, "")
        val timerPref = prefSettings.getString(TIMER_KEY, "")
        timerStarted = timerPref?.toBoolean() ?: false

        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.UK)
        val today = sdf.format(Date())
        // if it's a new day, reset the timer
        if (today != counterDate) {
            counterDate = today
            timerStarted = false
        }

        if (timerStarted) {
            timerButton.visibility = View.GONE
        } else {
            timerButton.visibility = View.VISIBLE
        }

        coinWallet = prefsToCoinList(WALLET_KEY)
        coinBank = prefsToCoinList(BANK_KEY)
        collectedCoins = prefsToStringList(COLLECTED_KEY)
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }
    override fun onPause() {
        super.onPause()
        mapView?.onPause()

        listToPrefs(coinWallet, WALLET_KEY)
        listToPrefs(collectedCoins, COLLECTED_KEY)
        // All objects are from android.context.Context
        val prefSettings = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        // We need an Editor object to make preference changes
        val editor = prefSettings.edit()
        editor.putString(DOWNLOAD_KEY, downloadDate)
        editor.putString(JSON_KEY, lastJson)
        editor.putString(TIMER_KEY, timerStarted.toString())
        editor.apply()
    }

    /**
     * saves values to shared preferences
     */
    override fun onStop() {
        super.onStop()
        if (::locationLayerPlugin.isInitialized) {
            locationEngine.removeLocationUpdates()
            locationLayerPlugin.onStop()
        } else {
            Timber.d("locationLayerPlugin not initialized")
        }
        mapView?.onStop()
    }

    /**
     * is called at the end of the app's lifecycle, so we have to store everything onto firebase
     */
    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
        if (::locationLayerPlugin.isInitialized) {
            locationEngine.deactivate()
        } else {
            Timber.d("locationLayerPlugin not initialized")
        }

        // Restore preferences
        val prefSettings = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)

        // use "" as the default value (this might be the first time the app is run)
        downloadDate = prefSettings.getString(DOWNLOAD_KEY, "")
        lastJson = prefSettings.getString(JSON_KEY, "")
        val timerPref = prefSettings.getString(TIMER_KEY, "")
        timerStarted = timerPref?.toBoolean() ?: false
        gold = prefSettings.getString(GOLD_KEY, "")?.toDoubleOrNull()
        if (gold == null) {
            gold = 0.0
        }
        depositCounter = prefSettings.getString(COUNTER_KEY, "0")?.toIntOrNull()
        if (depositCounter == null) {
            depositCounter = 0
        }

        coinWallet = prefsToCoinList(WALLET_KEY)
        coinBank = prefsToCoinList(BANK_KEY)
        collectedCoins = prefsToStringList(COLLECTED_KEY)

        // uploads all information onto firestore,
        // so that when app is started again, firestore is correct
        uploadCoins(WALLET_KEY, coinWallet)
        uploadCoins(BANK_KEY, coinBank)
        uploadList(COLLECTED_KEY, collectedCoins)
        val document = firestore?.collection(COLLECTION_KEY)?.document(firebaseEmail)
        val data = HashMap<String, Any>()
        data[GOLD_KEY] = gold!!
        data[COUNTER_KEY] = depositCounter!!
        data[DOWNLOAD_KEY] = downloadDate!!
        data[TIMER_KEY] = timerStarted
        document?.set(data, SetOptions.merge())
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

