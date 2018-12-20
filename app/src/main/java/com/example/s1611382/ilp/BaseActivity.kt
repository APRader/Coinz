package com.example.s1611382.ilp

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import com.google.common.reflect.TypeToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import java.lang.reflect.Type

abstract class BaseActivity : AppCompatActivity() {

    companion object {
        const val COIN_WALLET = "coinWallet"
        const val RATES = "rates"
        // keys are identifiers for firestore and shared preferences
        const val COLLECTION_KEY = "Users"
        const val WALLET_KEY = "Wallet"
        const val BANK_KEY = "Bank"
        const val COLLECTED_KEY = "CollectedCoins"
        const val DOWNLOAD_KEY = "DownloadDate"
        const val JSON_KEY = "JSON"
        const val TRADING_KEY = "Trading"
        const val GOLD_KEY = "Gold"
        const val COUNTER_KEY = "DepositCounter"
        const val LAST_DATE_KEY = "LastDate"
        const val COLLECTIBLES_KEY = "Collectibles"
        const val TIMER_KEY = "TimerStarted"
        const val PREF_FILE = "MyPrefsFile"
        const val COIN_LIST = "coinList"
        const val SELECTION_KEY = "selectionKey"
        const val TEXT_KEY = "textKey"
        // keys used to identify what action the selectionFragment should do
        const val DEPOSIT_SELECTION = "deposit"
        const val CONVERSION_SELECTION = "conversion"
        // user can deposit at most 25 collected coins per day
        const val DEPOSIT_LIMIT = 25
        // how many milliseconds the timer lasts
        const val TIMER: Long = 900000
        const val INTERVAL: Long = 1000
        const val COLLECTIBLE_PRICE: Int = 10000
    }

    open fun setToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar: ActionBar? = supportActionBar
        // creates up navigation to get back to parent activity (main map)
        actionbar?.setDisplayHomeAsUpEnabled(true)
        }

    /**
     * get firestore instance
     */
    fun firestoreSetup(): FirebaseFirestore? {
        val firestore = FirebaseFirestore.getInstance()
        val firebaseSettings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build()
        firestore.firestoreSettings = firebaseSettings
        return firestore
    }

    /**
     * update Firestore with contents of local coinList
     */
    fun uploadCoins(key: String, coinList: ArrayList<Coin>) {
        val firestore = firestoreSetup()
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email.toString()

        val gson = Gson()
        val json = gson.toJson(coinList)
        val data = HashMap<String, Any>()
        data[key] = json
        firestore?.collection(COLLECTION_KEY)
                ?.document(email)
                ?.set(data, SetOptions.merge())
    }

    /**
     * update Firestore with contents of local list of strings
     */
    fun uploadList(key: String, list: ArrayList<String>) {
        val firestore = firestoreSetup()
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email.toString()

        val gson = Gson()
        val json = gson.toJson(list)
        firestore?.collection(COLLECTION_KEY)
                ?.document(email)
                ?.update(key, json)
    }

    /**
     * update Firestore with contents of local array of Boolean
     */
    fun uploadBooleanArray(key: String, array: Array<Boolean>) {
        val firestore = firestoreSetup()
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email.toString()

        val gson = Gson()
        val json = gson.toJson(array)
        firestore?.collection(COLLECTION_KEY)
                ?.document(email)
                ?.update(key, json)
    }

    /**
     * stores a list in shared preferences as json string, with the given key
     */
    fun listToPrefs(list: ArrayList<*>, prefsKey: String) {
        val prefSettings = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        val editor = prefSettings.edit()
        val gson = Gson()
        val json = gson.toJson(list)
        editor.putString(prefsKey, json)
        editor.apply()
    }

    /**
     * stores an array of Boolean in shared preferences as json string, with the given key
     */
    fun collectiblesArrayToPrefs(collectibles: Array<Boolean>, prefsKey: String) {
        val prefSettings = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        val editor = prefSettings.edit()
        val gson = Gson()
        val json = gson.toJson(collectibles)
        editor.putString(prefsKey, json)
        editor.apply()
    }

    /**
     * puts json string from shared preferences into a list of coins, given key
     * returns the list
     */
    fun prefsToCoinList(prefsKey: String): ArrayList<Coin> {
        val prefSettings = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        val gson= Gson()
        val json = prefSettings.getString(prefsKey, "[]")
        val type: Type = object : TypeToken<ArrayList<Coin>>() {}.type
        return gson.fromJson<ArrayList<Coin>>(json, type)
    }

    /**
     * puts json string from shared preferences into a list of strings, given key
     * returns the list
     */
    fun prefsToStringList(prefsKey: String): ArrayList<String> {
        val prefSettings = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        val gson= Gson()
        val json = prefSettings.getString(prefsKey, "[]")
        val type = object : TypeToken<ArrayList<String>>() {}.type
        return gson.fromJson(json, type)
    }

    /**
     * puts json string from shared preferences into an array of Booleans, given key
     * returns the array
     */
    fun prefsToCollectiblesArray(prefsKey: String): Array<Boolean> {
        val prefSettings = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefSettings.getString(prefsKey, "[]")
        // array of size 30 with all entries being false
        var collectibles: Array<Boolean> = Array(30) {false}
        if (json != "[]") {
            val type = object : TypeToken<Array<Boolean>>() {}.type
            collectibles = gson.fromJson(json, type)
        }
        return collectibles
    }

    /**
     * returns value for gold from shared preferences
     */
    fun prefsToGold(): Double {
        val prefSettings = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        var gold = prefSettings.getString(GOLD_KEY, "")?.toDoubleOrNull()
        if (gold == null) {
            gold = 0.0
        }
        return gold
    }

    /**
     * returns value for depositCounter from shared preferences
     */
    fun prefsToDepositCounter(): Int {
        val prefSettings = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        var depositCounter = prefSettings.getString(COUNTER_KEY, "0")?.toIntOrNull()
        if (depositCounter == null) {
            depositCounter = 0
        }
        return depositCounter
    }

    /**
     * returns value for timerStarted from shared preferences
     */
    fun prefsToTimerStarted(): Boolean {
        val prefSettings = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        val timerPref = prefSettings.getString(TIMER_KEY, "")
        return timerPref?.toBoolean() ?: false
    }

    /**
     * returns value for downloadDate from shared preferences
     */
    fun prefsToDownloadDate(): String? {
        val prefSettings = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        return prefSettings.getString(DOWNLOAD_KEY, "")
    }

    /**
     * returns value for lastJson from shared preferences
     */
    fun prefsToLastJson(): String? {
        val prefSettings = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        return prefSettings.getString(JSON_KEY, "")
    }

    /**
     * returns value for lastDate from shared preferences
     */
    fun prefsToLastDate(): String? {
        val prefSettings = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        return prefSettings.getString(LAST_DATE_KEY, "")
    }

    /**
     * turns a firestore document into a coin that is traded
     */
    fun documentToTradedCoin(document: QueryDocumentSnapshot): Coin {
        val data = document.data
        return Coin(id = data["id"].toString(),
                value = data["value"].toString().toFloat(),
                currency = data["currency"].toString(), traded = 1)
    }

    /**
     * turns lists that are stored as strings into [] if they are null
     */
    fun nullList(list: String): String {
        var newList = list
        if (list == "null") { newList = "[]" }
        return newList
    }

}

