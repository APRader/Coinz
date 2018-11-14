package com.example.s1611382.ilp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.coin_text_view.view.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Bank: AppCompatActivity(), WalletFragment.OnCoinsDeposited {
    private val preferencesFile = "MyPrefsFile" // for storing preferences
    private var gold: Double? = 0.0
    private lateinit var rates: ArrayList<Coin>
    private lateinit var coinWallet: ArrayList<Coin>
    private var coinBank : ArrayList<Coin> = arrayListOf()

    companion object {
        val COINWALLET = "coinWallet"
        val COINBANK = "coinBank"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bank)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar: ActionBar? = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)

        rates = intent.extras.getParcelableArrayList(MainActivity.RATES)
        coinWallet = intent.extras.getParcelableArrayList(MainActivity.COINWALLET)

        val ratesView: TextView = findViewById(R.id.rates_id)
        ratesView.text = rates.toString()

        val depositButton: Button = findViewById(R.id.deposit_button_id)
        depositButton.setOnClickListener {
            //gold = gold?.plus(1)
            //val goldView: TextView = findViewById(R.id.gold_id)
            //goldView.text = "Your GOLD: " + gold.toString()

            // hiding deposit button so user can't add more than one WalletFragment
            depositButton.visibility = View.GONE

            //make fragment to show list of coins from which to choose from
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            val fragment = WalletFragment()
            val args = Bundle()
            args.putParcelableArrayList(COINWALLET, coinWallet)
            args.putParcelableArrayList(COINBANK, coinBank)
            fragment.arguments = args
            fragmentTransaction.replace(R.id.deposit_placeholder, fragment)
            // adding to stack so we can pop it when we finish depositing coins (or when user presses back button)
            fragmentTransaction.addToBackStack("WalletFragment")
            fragmentTransaction.commit()
        }
    }

    override fun onCoinsDeposited(depositedCoins: ArrayList<Coin>) {
        for (coin in depositedCoins) {
            coinWallet.remove(coin)
            coinBank.add(coin)
        }
        // removes last fragment from stack (which is WalletFragment)
        val fragmentManager = supportFragmentManager
        fragmentManager.popBackStack()
        // add deposit button again, so user can deposit again
        val depositButton: Button = findViewById(R.id.deposit_button_id)
        depositButton.visibility = View.VISIBLE
    }

    override fun onStart() {
        super.onStart()
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        gold = settings.getString("lastGold", "").toDoubleOrNull()
        if (gold == null) {
            gold = 0.0
        }
        val goldView: TextView = findViewById(R.id.gold_id)
        goldView.text = "Your GOLD: " + gold.toString()

        val gson= Gson()
        val json = settings.getString("lastCoinBank", "[]")
        val type = object : TypeToken<ArrayList<Coin>>() {}.type
        coinBank = gson.fromJson<ArrayList<Coin>>(json, type)
    }

    override fun onStop() {
         super.onStop()
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putString("lastGold", gold.toString())
        val gson = Gson()
        val json = gson.toJson(coinBank)
        editor.putString("lastCoinBank", json)
        editor.apply()
    }

    override fun onPause() {
        super.onPause()
        // need to change values in wallet shared prefs before onStart of map is called, because it uses wallet
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        val editor = settings.edit()
        val gson = Gson()
        val json = gson.toJson(coinWallet)
        editor.putString("lastCoinWallet", json)
        editor.apply()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // when user presses back button, fragment will be closed, so we need to set button that launched fragment to visible
        val depositButton: Button = findViewById(R.id.deposit_button_id)
        if (depositButton.visibility == View.GONE) {
            depositButton.visibility = View.VISIBLE
        }
    }

}