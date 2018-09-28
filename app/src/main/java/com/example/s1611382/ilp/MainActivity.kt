package com.example.s1611382.ilp

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AlertDialog
import android.widget.Button
import android.widget.FrameLayout
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
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.style.light.Position
import java.io.File


class MainActivity : AppCompatActivity(), PermissionsListener, LocationEngineListener {

    private lateinit var mapView: MapView
    private lateinit var map: MapboxMap
    private lateinit var walletButton: Button
    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var permissionManager: PermissionsManager
    //stores current location at all times
    private lateinit var originLocation: Location

    //gives user location
    private var locationEngine: LocationEngine? = null
    //for UI: icon representing user location
    private var locationLayerPlugin: LocationLayerPlugin? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Mapbox.getInstance(applicationContext, getString(R.string.access_token))
        mapView = findViewById(R.id.mapView)
        walletButton = findViewById(R.id.walletButton)
        mapView.onCreate(savedInstanceState)
        // this makes map variable usable in the rest of the class
        mapView.getMapAsync { mapboxMap ->
            map = mapboxMap
            enableLocation()

            drawCoinLocations(map)
        }

        walletButton.setOnClickListener {
            openWallet()
        }

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

    }

    //get user's permission for location
    private fun enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            initializeLocationEngine()
            initializeLocationLayer()
        } else {
            permissionManager = PermissionsManager(this)
            permissionManager.requestLocationPermissions(this)
        }
    }

    @SuppressLint("MissingPermission")
    private fun initializeLocationEngine() {
        locationEngine = LocationEngineProvider(this).obtainBestLocationEngineAvailable()
        locationEngine?.priority = LocationEnginePriority.HIGH_ACCURACY
        locationEngine?.addLocationEngineListener(this)
        locationEngine?.activate()

        val lastLocation = locationEngine?.lastLocation
        if (lastLocation != null) {
            originLocation = lastLocation
            setCameraPosition(lastLocation)
        }
    }

    private fun initializeLocationLayer() {
        locationLayerPlugin = LocationLayerPlugin(mapView, map, locationEngine)
        //for camera tracking location
        locationLayerPlugin?.setLocationLayerEnabled(true)
        locationLayerPlugin?.cameraMode = CameraMode.TRACKING
        locationLayerPlugin?.renderMode = RenderMode.NORMAL
    }

    private fun setCameraPosition(location: Location) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(location.latitude, location.longitude), 14.5))
    }

    private fun openWallet() {
        val intent = Intent(this, Wallet::class.java)
        startActivity(intent)
    }

    private fun drawCoinLocations(map: MapboxMap) {

        val featureCollection = FeatureCollection.fromJson("\n" +
                "{\n" +
                "  \"type\": \"FeatureCollection\",\n" +
                "  \"date-generated\": \"Fri Sep 28 2018\",\n" +
                "  \"time-generated\": \"00:00\",\n" +
                "  \"approximate-time-remaining\": \"23:59\",\n" +
                "  \"rates\": {\n" +
                "                   \"SHIL\": 52.31043747053524,\n" +
                "                   \"DOLR\": 22.808490581537793,\n" +
                "                   \"QUID\": 45.12756144948401,\n" +
                "                   \"PENY\": 28.379507202621213\n" +
                "               },\n" +
                "  \"features\": [\n" +
                "  \n" +
                "    {\n" +
                "      \"type\": \"Feature\",\n" +
                "      \n" +
                "      \"properties\": {\n" +
                "        \"id\": \"44b1-42cf-6bc8-9600-8d8c-aca3\",\n" +
                "        \"value\": \"6.9096963653866075\",\n" +
                "        \"currency\": \"PENY\",\n" +
                "        \"marker-symbol\": \"6\",\n" +
                "        \"marker-color\": \"#ff0000\"\n" +
                "      },\n" +
                "      \n" +
                "      \"geometry\": {\n" +
                "        \"type\": \"Point\",\n" +
                "        \"coordinates\": [\n" +
                "          -3.191763743472121,\n" +
                "          55.94588791509546\n" +
                "        ]\n" +
                "      }\n" +
                "\n" +
                "    },\n" +
                "        \n" +
                "    {\n" +
                "      \"type\": \"Feature\",\n" +
                "      \n" +
                "      \"properties\": {\n" +
                "        \"id\": \"7740-2cd5-e181-830a-8146-2c0b\",\n" +
                "        \"value\": \"8.529361506913553\",\n" +
                "        \"currency\": \"PENY\",\n" +
                "        \"marker-symbol\": \"8\",\n" +
                "        \"marker-color\": \"#ff0000\"\n" +
                "      },\n" +
                "      \n" +
                "      \"geometry\": {\n" +
                "        \"type\": \"Point\",\n" +
                "        \"coordinates\": [\n" +
                "          -3.190194725217555,\n" +
                "          55.94610689723401\n" +
                "        ]\n" +
                "      }\n" +
                "\n" +
                "    },\n" +
                "        \n" +
                "    {\n" +
                "      \"type\": \"Feature\",\n" +
                "      \n" +
                "      \"properties\": {\n" +
                "        \"id\": \"8c9e-41bc-2b5d-397b-4546-467d\",\n" +
                "        \"value\": \"0.35899518564213473\",\n" +
                "        \"currency\": \"SHIL\",\n" +
                "        \"marker-symbol\": \"0\",\n" +
                "        \"marker-color\": \"#0000ff\"\n" +
                "      },\n" +
                "      \n" +
                "      \"geometry\": {\n" +
                "        \"type\": \"Point\",\n" +
                "        \"coordinates\": [\n" +
                "          -3.1924307902888986,\n" +
                "          55.94551117206641\n" +
                "        ]\n" +
                "      }\n" +
                "\n" +
                "    }\n" +
                "        \n" +
                "   ]\n" +
                "}")

        val features = featureCollection.features()

        if (features != null) {
            for (f: Feature in features) {
                if (f.geometry() is Point) {
                    print("j")
                    val coordinates = (f.geometry() as Point).coordinates()
                    map.addMarker(
                            MarkerOptions().position(LatLng(coordinates[1], coordinates[0]))
                    )
                }

            }

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
        location?.let {
            originLocation = location
            //setCameraPosition(location)
        }
    }
    @SuppressLint("MissingPermission")
    override fun onConnected() {
        locationEngine?.requestLocationUpdates()
    }


    // lifecycle methods for MapBox override standard funs
    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            locationEngine?.requestLocationUpdates()
            locationLayerPlugin?.onStart()
        }
        mapView.onStart()
    }
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }
    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
    override fun onStop() {
        super.onStop()
        locationEngine?.removeLocationUpdates()
        locationLayerPlugin?.onStop()
        mapView.onStop()
    }
    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        locationEngine?.deactivate()
    }
    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (outState != null) {
            mapView.onSaveInstanceState(outState)
        }
    }
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
