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
import timber.log.Timber
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
        const val COUNTER_DATE_KEY = "CounterDate"
        const val PREF_FILE = "MyPrefsFile"
        const val COIN_LIST = "coinList"
        const val SELECTION_KEY = "selectionKey"
        const val TEXT_KEY = "textKey"
        // keys used to identify what action the selectionFragment should do
        const val DEPOSIT_SELECTION = "deposit"
        const val CONVERSION_SELECTION = "conversion"
        // user can deposit at most 25 collected coins per day
        const val DEPOSIT_LIMIT = 25
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
        firestore?.collection(COLLECTION_KEY)
                ?.document(email)
                ?.update(key, json)
    }

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
     * turns a firestore document into a coin
     */
    fun documentToCoin(document: QueryDocumentSnapshot): Coin {
        val data = document.data
        return Coin(id = data["id"].toString(),
                value = data["value"].toString().toFloat(),
                currency = data["currency"].toString())
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

