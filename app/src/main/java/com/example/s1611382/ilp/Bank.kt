package com.example.s1611382.ilp

import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.app.NavUtils
import android.support.v7.app.AlertDialog
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Bank: BaseActivity(), SelectionFragment.OnCoinsSelected {
    private var gold: Double? = 0.0
    private lateinit var rates: HashMap<String, Float>
    private var coinWallet: ArrayList<Coin> = arrayListOf()
    private var coinBank : ArrayList<Coin> = arrayListOf()

    private lateinit var depositButton: Button
    private lateinit var conversionButton: Button
    private lateinit var fragmentManager: FragmentManager

    // counts how many deposits have been made today
    private var depositCounter: Int? = 0
    private var counterDate: String? = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bank)
        setToolbar()
        @Suppress("UNCHECKED_CAST")
        rates = intent?.extras?.getSerializable(RATES) as HashMap<String, Float>
        coinWallet = intent?.extras?.getParcelableArrayList(COIN_WALLET)!!

        val ratesView: TextView = findViewById(R.id.rates_id)
        var ratesText = "No rates available"
        if (rates.isNotEmpty()) {
            val shil = rates["SHIL"]
            val dolr = rates["DOLR"]
            val quid = rates["QUID"]
            val peny = rates["PENY"]
            ratesText = "SHIL: $shil\nDOLR: $dolr\nQUID: $quid\nPENY: $peny"
        }

        ratesView.text = ratesText

        depositButton = findViewById(R.id.deposit_button_id)
        conversionButton = findViewById(R.id.conversion_button_id)
        fragmentManager = supportFragmentManager

        depositButton.setOnClickListener {
            openDepositFragment()
        }

        conversionButton.setOnClickListener {
            openConversionFragment()
        }
    }

    /**
     * creates a fragment that displays a SelectionFragment with user's wallet
     * user can choose their coins from a list and deposit them
     */
    private fun openDepositFragment() {
        // hiding deposit button so user can't add more than one WalletFragment
        depositButton.visibility = View.GONE

        // if the other fragment is open we need to close it and make its button visible
        fragmentManager.popBackStack()
        conversionButton.visibility = View.VISIBLE

        val fragmentTransaction = fragmentManager.beginTransaction()
        val fragment = SelectionFragment()
        val args = Bundle()
        args.putParcelableArrayList(COIN_LIST, coinWallet)
        args.putString(SELECTION_KEY, DEPOSIT_SELECTION)
        args.putString(TEXT_KEY, getString(R.string.coin_deposit))
        fragment.arguments = args
        fragmentTransaction.replace(R.id.deposit_placeholder, fragment)

        // adding to stack so we can pop it when user finishes depositing coins (or presses back button)
        fragmentTransaction.addToBackStack("WalletFragment")
        fragmentTransaction.commit()
    }

    /**
     * creates a fragment that displays a SelectionFragment with user's bank
     * user can choose their coins from a list and convert them into GOLD
     */
    private fun openConversionFragment() {
        // hiding convert button so user can't add more than one BankFragment
        conversionButton.visibility = View.GONE

        // if the other fragment is open we need to close it and make its button visible
        fragmentManager.popBackStack()
        depositButton.visibility = View.VISIBLE

        val fragmentTransaction = fragmentManager.beginTransaction()
        val fragment = SelectionFragment()
        val args = Bundle()
        args.putParcelableArrayList(COIN_LIST, coinBank)
        args.putString(SELECTION_KEY, CONVERSION_SELECTION)
        args.putString(TEXT_KEY, getString(R.string.coin_conversion))
        fragment.arguments = args
        fragmentTransaction.replace(R.id.conversion_placeholder, fragment)

        // adding to stack so we can pop it when user finishes depositing coins (or presses back button)
        fragmentTransaction.addToBackStack("BankFragment")
        fragmentTransaction.commit()
    }


    /**
     * deposits selected coins from wallet into bank
     * only if collected coins do not go over deposit limit
     * otherwise message is shown
     */
    private fun depositCoins(depositedCoins: ArrayList<Coin>?) {
        // stores the amount of coins that were not traded, as these do not contribute to the max deposit rule
        var untradedCoins = 0
        if (depositedCoins != null) {
            for (coin in depositedCoins) {
                // coin is not traded
                if (coin.traded == 0) {
                    untradedCoins++
                }
            }
        }

        val numCoins = untradedCoins + depositCounter!!
        if (numCoins <= DEPOSIT_LIMIT) {
            depositCounter = numCoins
            if (depositedCoins != null) {
                for (coin in depositedCoins) {
                    coinWallet.remove(coin)
                    coinBank.add(coin)
                }
            }
        } else {
            val coinsLeft = DEPOSIT_LIMIT - depositCounter!!
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.deposit_limit_title))
            if (coinsLeft == 0) {
                builder.setMessage(getString(R.string.deposit_limit_message_0))
            } else {
                builder.setMessage(String.format(getString(R.string.deposit_limit_message_many), untradedCoins, depositCounter, coinsLeft))
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
        // removes last fragment from stack (which is WalletFragment)
        val fragmentManager = supportFragmentManager
        fragmentManager.popBackStack()
        // re-add deposit button, so user can deposit again
        val depositButton: Button = findViewById(R.id.deposit_button_id)
        depositButton.visibility = View.VISIBLE
    }


    /**
     * converts coins from bank into GOLD using current rates
     */
    private fun convertCoins(convertedCoins: ArrayList<Coin>?) {
        if (rates.isEmpty()) {
            // happens if app couldn't download rates
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.rates_unavailable_title))
            builder.setMessage(getString(R.string.rates_unavailable_message))
            val dialog: AlertDialog = builder.create()
            dialog.show()
        } else {
            if (convertedCoins != null) {
                for (coin in convertedCoins) {
                    gold = gold?.plus(coin.value * rates[coin.currency]!!)
                    val goldView: TextView = findViewById(R.id.gold_id)
                    goldView.text = String.format(getString(R.string.gold), gold)
                    coinBank.remove(coin)
                }
            }
        }

        // removes last fragment from stack (which is BankFragment)
        val fragmentManager = supportFragmentManager
        fragmentManager.popBackStack()
        // re-add conversion button, so user can convert again
        val convertButton: Button = findViewById(R.id.conversion_button_id)
        convertButton.visibility = View.VISIBLE
    }

    /**
     * called when user clicks the action button in a fragment
     * opens function depending on which fragment user is in (using selection type)
     */
    override fun onCoinsSelected(selectedCoins: ArrayList<Coin>?, selectionType: String?) {
        when (selectionType) {
            DEPOSIT_SELECTION -> depositCoins(selectedCoins)
            CONVERSION_SELECTION -> convertCoins(selectedCoins)
        }
    }

    override fun onStart() {
        super.onStart()
        val settings = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        gold = settings.getString(GOLD_KEY, "")?.toDoubleOrNull()
        if (gold == null) {
            gold = 0.0
        }
        val goldView: TextView = findViewById(R.id.gold_id)
        goldView.text = String.format(getString(R.string.gold), gold)

        depositCounter = settings.getString(COUNTER_KEY, "0")?.toIntOrNull()
        if (depositCounter == null) {
            depositCounter = 0
        }

        counterDate = settings.getString(COUNTER_DATE_KEY, "")

        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.UK)
        val today = sdf.format(Date())
        // if it's a new day, reset the deposit counter
        if (today != counterDate) {
            counterDate = today
            depositCounter = 0
        }

        coinBank = prefsToCoinList(BANK_KEY)
    }


    /**
     * all values that could have been changed are stored in shared preferences
     */
    override fun onPause() {
        super.onPause()

        val settings = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putString(GOLD_KEY, gold.toString())
        editor.putString(COUNTER_KEY, depositCounter.toString())
        editor.putString(COUNTER_DATE_KEY, counterDate)
        editor.apply()
        listToPrefs(coinBank, BANK_KEY)
        listToPrefs(coinWallet, WALLET_KEY)
    }

    /**
     * override up navigation to behave like back navigation
     * This prevents map from being drawn again.
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        onBackPressed()
        return true
    }

    /**
     * when user presses back button, fragment will be closed,
     * so we need to set button that launched fragment to visible
     */
    override fun onBackPressed() {
        val depositButton: Button = findViewById(R.id.deposit_button_id)
        val convertButton: Button = findViewById(R.id.conversion_button_id)
        depositButton.visibility = View.VISIBLE
        convertButton.visibility = View.VISIBLE

        super.onBackPressed()
    }

}