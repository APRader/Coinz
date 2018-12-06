package com.example.s1611382.ilp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.coin_text_view.view.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Bank: AppCompatActivity(), SelectionFragment.OnCoinsSelected {
    private val preferencesFile = "MyPrefsFile" // for storing preferences
    private var gold: Double? = 0.0
    private lateinit var rates: HashMap<String, Float>
    private lateinit var coinWallet: ArrayList<Coin>
    private var coinBank : ArrayList<Coin> = arrayListOf()
    // keys used to identify what action the selectionFragment should do
    private val depositSelection = "deposit"
    private val convertSelection = "convert"

    // counts how many deposits have been made today
    private var depositCounter: Int? = 0
    private var counterDate = ""

    companion object {
        val COINLIST = "coinList"
        val SELECTIONKEY = "selectionKey"
        val TEXTKEY = "textKey"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bank)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar: ActionBar? = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)

        rates = intent.extras.getSerializable(MainActivity.RATES) as HashMap<String, Float>
        coinWallet = intent.extras.getParcelableArrayList(MainActivity.COINWALLET)

        val ratesView: TextView = findViewById(R.id.rates_id)
        ratesView.text = rates.toString()

        val depositButton: Button = findViewById(R.id.deposit_button_id)
        val convertButton: Button = findViewById(R.id.convert_button_id)

        depositButton.setOnClickListener {
            // hiding deposit button so user can't add more than one WalletFragment
            depositButton.visibility = View.GONE

            //make fragment to show list of coins from which to choose from
            val fragmentManager = supportFragmentManager

            // if the other fragment is open we need to close it and make its button visible
            fragmentManager.popBackStack()
            convertButton.visibility = View.VISIBLE

            val fragmentTransaction = fragmentManager.beginTransaction()
            val fragment = SelectionFragment()
            val args = Bundle()
            args.putParcelableArrayList(COINLIST, coinWallet)
            args.putString(SELECTIONKEY, depositSelection)
            args.putString(TEXTKEY, "Deposit selected coins")
            fragment.arguments = args
            fragmentTransaction.replace(R.id.deposit_placeholder, fragment)
            // adding to stack so we can pop it when we finish depositing coins (or when user presses back button)
            fragmentTransaction.addToBackStack("WalletFragment")
            fragmentTransaction.commit()
        }

        convertButton.setOnClickListener {
            // hiding convert button so user can't add more than one BankFragment
            convertButton.visibility = View.GONE

            //make fragment to show list of coins from which to choose from
            val fragmentManager = supportFragmentManager

            // if the other fragment is open we need to close it and make its button visible
            fragmentManager.popBackStack()
            depositButton.visibility = View.VISIBLE

            val fragmentTransaction = fragmentManager.beginTransaction()
            val fragment = SelectionFragment()
            val args = Bundle()
            args.putParcelableArrayList(COINLIST, coinBank)
            args.putString(SELECTIONKEY, convertSelection)
            args.putString(TEXTKEY, "Convert selected coins")
            fragment.arguments = args
            fragmentTransaction.replace(R.id.convert_placeholder, fragment)
            // adding to stack so we can pop it when we finish depositing coins (or when user presses back button)
            fragmentTransaction.addToBackStack("BankFragment")
            fragmentTransaction.commit()
        }
    }

    private fun depositCoins(depositedCoins: ArrayList<Coin>) {
        // TODO: Traded coins do not affect count

        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.UK)
        val today = sdf.format(Date())
        // if it's a new day, reset the deposit counter
        if (today != counterDate) {
            counterDate = today
            depositCounter = 0
        }
        val numCoins = depositedCoins.size + depositCounter!!
        if (numCoins <= 25) {
            depositCounter = depositCounter!! + depositedCoins.size
            for (coin in depositedCoins) {
                coinWallet.remove(coin)
                coinBank.add(coin)
            }
        } else {
            val coinsLeft = 25 - depositCounter!!
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Deposit limit reached")
            if (coinsLeft == 0) {
                builder.setMessage("You have reached the limit of 25 coins deposited today. " +
                        "You can deposit coins again tomorrow.")
            } else {
                builder.setMessage("You are trying to deposit ${depositedCoins.size} coins, " +
                        "but you have already deposited $depositCounter coins today. " +
                        "You can only deposit a maximum of 25 coins per day, " +
                        "please select at most $coinsLeft to deposit.")
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
        // removes last fragment from stack (which is WalletFragment)
        val fragmentManager = supportFragmentManager
        fragmentManager.popBackStack()
        // add deposit button again, so user can deposit again
        val depositButton: Button = findViewById(R.id.deposit_button_id)
        depositButton.visibility = View.VISIBLE
    }

    private fun convertCoins(convertedCoins: ArrayList<Coin>) {
        if (rates.isEmpty()) {
            // happens if app couldn't download rates
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Exchange rates unavailable")
            builder.setMessage("Coins cannot be converted. " +
                    "Please go to the map with a working internet connection, " +
                    "so rates can be downloaded.")
            val dialog: AlertDialog = builder.create()
            dialog.show()
        } else {
            for (coin in convertedCoins) {
                gold = gold?.plus(coin.value * rates[coin.currency]!!)
                val goldView: TextView = findViewById(R.id.gold_id)
                goldView.text = "Your GOLD: " + gold.toString()
                coinBank.remove(coin)
            }
        }

        // removes last fragment from stack (which is BankFragment)
        val fragmentManager = supportFragmentManager
        fragmentManager.popBackStack()
        // add convert button again, so user can deposit again
        val convertButton: Button = findViewById(R.id.convert_button_id)
        convertButton.visibility = View.VISIBLE
    }

    override fun onCoinsSelected(selectedCoins: ArrayList<Coin>, selectionType: String) {
        when (selectionType) {
            depositSelection -> depositCoins(selectedCoins)
            convertSelection -> convertCoins(selectedCoins)
        }
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

        depositCounter = settings.getString("lastDepositCounter", "0").toIntOrNull()
        if (depositCounter == null) {
            depositCounter = 0
        }
        counterDate = settings.getString("lastCounterDate", "")

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
        editor.putString("lastDepositCounter", depositCounter.toString())
        editor.putString("lastCounterDate", counterDate)
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
        val convertButton: Button = findViewById(R.id.convert_button_id)
        depositButton.visibility = View.VISIBLE
        convertButton.visibility = View.VISIBLE
    }

}