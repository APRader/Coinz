package com.example.s1611382.ilp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Bank: BaseActivity(), SelectionFragment.OnCoinsSelected {
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

    private var firestore: FirebaseFirestore? = null
    private val COLLECTION_KEY = "Users"
    private val WALLET_KEY = "Wallet"
    private val BANK_KEY = "Bank"
    private val TAG = "Bank"

    companion object {
        val COINLIST = "coinList"
        val SELECTIONKEY = "selectionKey"
        val TEXTKEY = "textKey"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bank)
        setToolbar()

        rates = intent.extras.getSerializable(RATES) as HashMap<String, Float>
        coinWallet = intent.extras.getParcelableArrayList(COIN_WALLET)

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
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.UK)
        val today = sdf.format(Date())
        // if it's a new day, reset the deposit counter
        if (today != counterDate) {
            counterDate = today
            depositCounter = 0
        }
        // stores the amount of coins that were not traded, as these do not contribute to the max deposit rule
        var untradedCoins = 0
        for (coin in depositedCoins) {
            // coin is not traded
            if (coin.traded == 0) {
                untradedCoins++
            }
        }
        val numCoins = untradedCoins + depositCounter!!
        if (numCoins <= 25) {
            depositCounter = numCoins
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
                builder.setMessage("You are trying to deposit ${untradedCoins} coins that you collected yourself, " +
                        "but you have already deposited $depositCounter coins today. " +
                        "You can only deposit a maximum of 25 coins per day, " +
                        "please select at most $coinsLeft to deposit, or receive coins from a trade, " +
                        "as these do not count towards the deposit limit.")
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
                goldView.text = "Your GOLD: ${gold?.toInt()}"
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

        firestore = FirebaseFirestore.getInstance()
        // Use com.google.firebase.Timestamp objects instead of java.util.Date objects
        val firebaseSettings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build()
        firestore?.firestoreSettings = firebaseSettings

        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email.toString()

        firestore?.collection(COLLECTION_KEY)
                ?.document(email)
                ?.get()
                ?.addOnSuccessListener { document ->
                    gold = document.data?.get("gold") as Double?
                    depositCounter = document.data?.get("counter").toString().toInt()
                    if (gold == null) {
                        gold = 0.0
                    }
                    val goldView: TextView = findViewById(R.id.gold_id)
                    goldView.text = "Your GOLD: ${gold?.toInt()}"
                    if (depositCounter == null) {
                        depositCounter = 0
                    }
                }

        counterDate = settings.getString("lastCounterDate", "")

        val bankCollection = firestore?.collection(COLLECTION_KEY)
                ?.document(email)
                ?.collection(BANK_KEY)

        bankCollection?.get()
                ?.addOnSuccessListener { documents ->
                    for (document in documents) {
                        val data = document.data
                        //TODO: catch if they are not right type
                        // traded value in coin is set to 1
                        val coin = Coin(id = data["id"].toString(),
                                value = data["value"].toString().toFloat(),
                                currency = data["currency"].toString())
                        coinBank.add(coin)
                    }
                }
                ?.addOnFailureListener { exception ->
                    print(exception)
                }
    }

    override fun onStop() {
        super.onStop()

        firestore = FirebaseFirestore.getInstance()
        // Use com.google.firebase.Timestamp objects instead of java.util.Date objects
        val firebaseSettings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build()
        firestore?.firestoreSettings = firebaseSettings

        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email.toString()

        val bankCollection = firestore?.collection(COLLECTION_KEY)
                ?.document(email)
                ?.collection(BANK_KEY)

        // delete all coins in firebase,
        bankCollection?.get()
                ?.addOnSuccessListener { documents ->
                    for (document in documents) {
                        document.reference.delete()
                                .addOnSuccessListener { print("success") }
                                .addOnFailureListener{ print(":(")}
                    }
                    // add current coins into firebase
                    for (coin in coinBank) {
                        bankCollection
                                ?.add(coin)
                                ?.addOnSuccessListener { Log.d(TAG, "Sent coin $coin") }
                                ?.addOnFailureListener { e -> Log.e(TAG, e.message) }
                    }
                }
                ?.addOnFailureListener {exception ->
                    print(exception)
                }
        val info = HashMap<String, Any>()
        info["gold"] = gold!!
        info["counter"] = depositCounter!!
        firestore?.collection(COLLECTION_KEY)
                ?.document(email)
                ?.set(info)

        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putString("lastCounterDate", counterDate)
        editor.apply()
    }

    override fun onPause() {
        super.onPause()
        // need to change values in wallet shared prefs before onStart of map is called, because it uses wallet
        firestore = FirebaseFirestore.getInstance()
        // Use com.google.firebase.Timestamp objects instead of java.util.Date objects
        val firebaseSettings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build()
        firestore?.firestoreSettings = firebaseSettings

        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email.toString()

        val walletCollection = firestore?.collection(COLLECTION_KEY)
                ?.document(email)
                ?.collection(WALLET_KEY)

        // delete all coins in firebase,
        walletCollection?.get()
                ?.addOnSuccessListener { documents ->
                    for (document in documents) {
                        document.reference.delete()
                                .addOnSuccessListener { print("success") }
                                .addOnFailureListener{ print(":(")}
                    }
                    // add current coins into firebase
                    for (coin in coinWallet) {
                        walletCollection
                                ?.add(coin)
                                ?.addOnSuccessListener { Log.d(TAG, "Sent coin $coin") }
                                ?.addOnFailureListener { e -> Log.e(TAG, e.message) }
                    }
                }
                ?.addOnFailureListener {exception ->
                    print(exception)
                }
    }

    override fun onBackPressed() {
        // when user presses back button, fragment will be closed, so we need to set button that launched fragment to visible
        val depositButton: Button = findViewById(R.id.deposit_button_id)
        val convertButton: Button = findViewById(R.id.convert_button_id)
        depositButton.visibility = View.VISIBLE
        convertButton.visibility = View.VISIBLE
        val yeet = supportFragmentManager

        // because wallet can be changed, the map has to be redrawn
        // back navigation would not call onMapReady again on the map activity
        // therefore we call upNavigation instead of back navigation
        // only open map if no fragment is open
        if (yeet.backStackEntryCount == 0) {
            NavUtils.navigateUpFromSameTask(this)
        }

        super.onBackPressed()
    }

}