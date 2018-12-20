package com.example.s1611382.ilp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class Trading : BaseActivity(), SelectionFragment.OnCoinsSelected{
    private var coinWallet: ArrayList<Coin> = arrayListOf()
    private lateinit var tradeButton: Button
    private lateinit var receiveButton: Button

    private lateinit var fragmentManager: FragmentManager
    private val tradeSelection = "trade"
    private var recipient = ""

    // counts how many deposits have been made today
    private var depositCounter: Int? = 0
    private var lastDate: String? = ""

    private var firestore: FirebaseFirestore? = null
    private var firebaseUser: FirebaseUser? = null
    private var firebaseEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?   ) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.trading)
        setToolbar()

        fragmentManager = supportFragmentManager

        coinWallet = intent?.extras?.getParcelableArrayList(COIN_WALLET)!!

        firestore = firestoreSetup()
        firebaseUser = FirebaseAuth.getInstance().currentUser
        firebaseEmail = firebaseUser?.email.toString()

        tradeButton = findViewById(R.id.trade_button_id)
        receiveButton = findViewById(R.id.receive_button_id)
        tradeButton.setOnClickListener{_ -> getRecipient() }
        receiveButton.setOnClickListener{_ -> receiveCoins() }
    }

    /**
     * opens dialogue for user to type in recipient's email
     * when user confirms, method chooseCoins is called
     * when user cancels, dialogue is dismissed
     */
    @SuppressLint("InflateParams")
    private fun getRecipient() {
        // user can only trade spare change, i.e. they have deposited 25 coins
        if (depositCounter!! >= 25) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.get_recipient))
            // taking root as null is ok for AlertDialog, as it does not expose root view
            val view = layoutInflater.inflate(R.layout.trade_dialog, null)
            val editText = view.findViewById(R.id.trade_edit_id) as EditText
            builder.setView(view)

            builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
                val userInput = editText.text
                if (!userInput.isBlank()) {
                    recipient = userInput.toString()
                    val email = firebaseEmail
                    if (email != recipient) {
                        chooseCoins()
                    } else {
                        val negativeBuilder = android.support.v7.app.AlertDialog.Builder(this)
                        negativeBuilder.setTitle(getString(R.string.self_trade_title))
                        negativeBuilder.setMessage(getString(R.string.self_trade_message))
                        val negativeDialog: android.support.v7.app.AlertDialog = negativeBuilder.create()
                        negativeDialog.show()
                    }

                }
                dialog.dismiss()
            }

            builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }

            builder.show()
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.spare_change_title))
            builder.setMessage(getString(R.string.spare_change_message))
            builder.show()
        }
    }

    /**
     * uses Firebase to send coins to another user
     * Each document in the User collection of firestore represents a user
     * In the user's trading collection, every document represents a coin the user got
     * Thus, we add coins to the trading collection of the recipient's document
     */
    private fun sendCoins(recipient: String?, sentCoins: ArrayList<Coin>?) {
        if (recipient != null) {
            if (firebaseUser != null ) {
                val collection = firestore?.collection(COLLECTION_KEY)
                        ?.document(recipient)
                        ?.collection(TRADING_KEY)
                if (sentCoins != null) {
                    for (coin in sentCoins) {
                        // add coin to recipient's trading collection
                        collection
                                ?.add(coin)
                                ?.addOnSuccessListener { Timber.d("Traded coin $coin") }
                                ?.addOnFailureListener { e -> Timber.e(e.message) }

                        // remove coin from sender's wallet
                        coinWallet.remove(coin)
                    }
                }
            }
        }
        // removes last fragment from stack (which is WalletFragment)
        fragmentManager.popBackStack()
        // re-add trade button, so user can trade again
        tradeButton.visibility = View.VISIBLE
    }

    /**
     * checks user's trading folder to see if someone has sent coins to them
     * displays message with how many coins user received
     */
    private fun receiveCoins() {
        // if the trade fragment is open we need to close it and make its button visible
        fragmentManager.popBackStack()
        tradeButton.visibility = View.VISIBLE

        var counter = 0

        val collection = firestore?.collection(COLLECTION_KEY)
                ?.document(firebaseEmail)
                ?.collection(TRADING_KEY)

        collection?.get()
                ?.addOnSuccessListener { documents ->
                    for (document in documents) {
                        val coin = documentToTradedCoin(document)
                        document.reference.delete()
                        coinWallet.add(coin)
                        counter++
                    }
                    val builder = android.support.v7.app.AlertDialog.Builder(this)
                    when (counter) {
                        0 -> { builder.setTitle(getString(R.string.no_coins_title))
                            builder.setMessage(getString(R.string.no_coins_message))}
                        1 -> { builder.setTitle(getString(R.string.one_coin_title))
                            builder.setMessage(getString(R.string.one_coin_message))}
                        else -> { builder.setTitle(getString(R.string.many_coins_title))
                            builder.setMessage(String.format(getString(R.string.many_coins_message), counter))}
                    }
                    val dialog: android.support.v7.app.AlertDialog = builder.create()
                    dialog.show()
                }
                ?.addOnFailureListener {exception ->
                    print(exception)
                }
    }

    /**
     * uses SelectionFragment to let user choose coins to send
     */
    private fun chooseCoins() {
        // hiding deposit button so user can't add more than one WalletFragment
        tradeButton.visibility = View.GONE

        //make fragment to show list of coins from which to choose from
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val fragment = SelectionFragment()
        val args = Bundle()
        args.putParcelableArrayList(COIN_LIST, coinWallet)
        args.putString(SELECTION_KEY, tradeSelection)
        args.putString(TEXT_KEY, "Send selected coins")
        fragment.arguments = args
        fragmentTransaction.replace(R.id.selection_placeholder, fragment)
        // adding to stack so we can pop it when we finish depositing coins (or when user presses back button)
        fragmentTransaction.addToBackStack("TradeFragment")
        fragmentTransaction.commit()
    }

    /**
     * gets called when user has confirmed which coins they want to send
     */
    override fun onCoinsSelected(selectedCoins: ArrayList<Coin>?, selectionType: String?) {
        when (selectionType) {
            tradeSelection -> sendCoins(recipient, selectedCoins)
        }
    }

    /**
     * restores shared preferences
     */
    override fun onStart() {
        super.onStart()
        depositCounter = prefsToDepositCounter()
        lastDate = prefsToLastDate()

        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.UK)
        val today = sdf.format(Date())
        // if it's a new day, reset the deposit counter
        if (today != lastDate) {
            lastDate = today
            depositCounter = 0
        }
    }

    /**
     * saves in shared preferences and firestore
     */
    override fun onPause() {
        super.onPause()

        listToPrefs(coinWallet, WALLET_KEY)
        val settings = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putString(COUNTER_KEY, depositCounter.toString())
        editor.putString(LAST_DATE_KEY, lastDate)
        editor.apply()

        uploadCoins(WALLET_KEY, coinWallet)
        val document = firestore?.collection(COLLECTION_KEY)?.document(firebaseEmail)
        val data = HashMap<String, Any>()
        data[COUNTER_KEY] = depositCounter!!
        data[LAST_DATE_KEY] = lastDate!!
        document?.set(data, SetOptions.merge())
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
     *  when user presses back button, fragment will be closed,
     *  so we need to set button that launched fragment to visible
     */
    override fun onBackPressed() {
        super.onBackPressed()
       tradeButton.visibility = View.VISIBLE
    }
}