package com.example.s1611382.ilp

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class Trading : BaseActivity(), SelectionFragment.OnCoinsSelected{
    private val preferencesFile = "MyPrefsFile" // for storing preferences
    private val RC_SIGN_IN = 123
    private var firestore: FirebaseFirestore? = null
    private var users: CollectionReference? = null
    private var firestoreTrading: DocumentReference? = null
    private lateinit var coinWallet: ArrayList<Coin>
    private lateinit var tradeButton: Button
    private lateinit var receiveButton: Button
    private val tradeSelection = "trade"
    private var recipient = ""

    companion object {
        private const val TAG = "Coinz"
        private const val COLLECTION_KEY = "Users"
        private const val TRADING_KEY = "Trading"
        private const val WALLET_KEY = "Wallet"
        private const val DOCUMENT_KEY = "Message"
        private const val NAME_FIELD = "Name"
        private const val COIN_FIELD = "Coin"
        val COINLIST = "coinList"
    }

    override fun onCreate(savedInstanceState: Bundle?   ) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.trading)
        setToolbar()

        coinWallet = intent.extras.getParcelableArrayList(COIN_WALLET)

        firestore = FirebaseFirestore.getInstance()
        // Use com.google.firebase.Timestamp objects instead of java.util.Date objects
        val settings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build()
        firestore?.firestoreSettings = settings
        //realtimeUpdateListener()

        tradeButton = findViewById(R.id.trade_button_id)
        receiveButton = findViewById(R.id.receive_button_id)
        tradeButton.setOnClickListener{_ -> getRecipient() }
        receiveButton.setOnClickListener{_ -> receiveCoins() }
    }

    private fun getRecipient() {
        // opens dialogue for user to name recipient's email
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Who do you want to send it to?")
        val view = layoutInflater.inflate(R.layout.trade_dialog, null)
        val editText = view.findViewById(R.id.trade_edit_id) as EditText
        builder.setView(view)

        builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
            val userInput = editText.text
            if (!userInput.isBlank()) {
                recipient = userInput.toString()
                val user = FirebaseAuth.getInstance().currentUser
                val email = user?.email.toString()
                if (email != recipient) {
                    chooseCoins()
                } else {
                    val builder = android.support.v7.app.AlertDialog.Builder(this)
                    builder.setTitle("You can't trade coins with yourself")
                    builder.setMessage("Try getting a friend")
                    val dialog: android.support.v7.app.AlertDialog = builder.create()
                    dialog.show()
                }

            }
            dialog.dismiss()
        }

        builder.setNegativeButton(android.R.string.cancel) {dialog, p1 ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun sendCoins(recipient: String?, sentCoins: ArrayList<Coin>?) {
        if (recipient != null) {
            val user = FirebaseAuth.getInstance().currentUser
            // Each document in the User collection of firestore represents a user
            // In the user's trading collection, every document represents a coin the user got
            // Thus, we add coins to the trading collection of the recipient's document
            if (user != null ) {
                val collection = firestore?.collection(COLLECTION_KEY)
                        ?.document(recipient)
                        ?.collection(TRADING_KEY)
                if (sentCoins != null) {
                    for (coin in sentCoins) {
                        // add coin to recipient's trading collection
                        collection
                                ?.add(coin)
                                ?.addOnSuccessListener { Log.d(TAG, "Sent coin $coin") }
                                ?.addOnFailureListener { e -> Log.e(TAG, e.message) }

                        // remove coin from sender's wallet
                        coinWallet.remove(coin)
                    }
                }
            }
        }
        // removes last fragment from stack (which is WalletFragment)
        val fragmentManager = supportFragmentManager
        fragmentManager.popBackStack()
        // add deposit button again, so user can deposit again
        tradeButton.visibility = View.VISIBLE
    }

    private fun receiveCoins() {
        // if the trade fragment is open we need to close it and make its button visible
        val fragmentManager = supportFragmentManager
        fragmentManager.popBackStack()
        tradeButton.visibility = View.VISIBLE

        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email.toString()

        var counter = 0

        val collection = firestore?.collection(COLLECTION_KEY)
                ?.document(email)
                ?.collection(TRADING_KEY)

        collection?.get()
                ?.addOnSuccessListener { documents ->
                    for (document in documents) {
                        val data = document.data
                        //TODO: catch if they are not right type
                        // traded value in coin is set to 1
                        val coin = Coin(id = data["id"].toString(),
                                value = data["value"].toString().toFloat(),
                                currency = data["currency"].toString(),
                                traded = 1)
                        document.reference.delete()
                                .addOnSuccessListener { print("success") }
                                .addOnFailureListener{ print(":(")}
                        coinWallet.add(coin)
                        counter++
                    }
                    val builder = android.support.v7.app.AlertDialog.Builder(this)
                    when (counter) {
                        0 -> { builder.setTitle("No coins for you.")
                            builder.setMessage("No one has sent you coins since the last time you checked :(.")}
                        1 -> { builder.setTitle("One coin for you.")
                            builder.setMessage("You received one coin.")}
                        else -> { builder.setTitle("You got coins!")
                            builder.setMessage("You received $counter coins.")}
                    }
                    val dialog: android.support.v7.app.AlertDialog = builder.create()
                    dialog.show()
                }
                ?.addOnFailureListener {exception ->
                    print(exception)
                }
    }
    //firestoreTrading?.set(newTrade)
    //       ?.addOnSuccessListener { print("Trade sent!") }
    //      ?.addOnFailureListener { e -> Log.e(TAG, e.message) }

    /*
    private fun realtimeUpdateListener() {
        users?.addSnapshotListener { documentSnapshot, e ->
            when {
                e!= null -> Log.e(TAG, e.message)
                documentSnapshot != null && documentSnapshot.exists() -> {
                    with(documentSnapshot) {
                        val incoming = "${data?.get(NAME_FIELD)}:" +
                                "${data?.get(COIN_FIELD)}"
                        print(incoming)
                    }
                }
            }
        }
    }*/

    private fun chooseCoins() {
        // hiding deposit button so user can't add more than one WalletFragment
        tradeButton.visibility = View.GONE

        //make fragment to show list of coins from which to choose from
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val fragment = SelectionFragment()
        val args = Bundle()
        args.putParcelableArrayList(COINLIST, coinWallet)
        args.putString(SELECTION_KEY, tradeSelection)
        args.putString(TEXT_KEY, "Send selected coins")
        fragment.arguments = args
        fragmentTransaction.replace(R.id.selection_placeholder, fragment)
        // adding to stack so we can pop it when we finish depositing coins (or when user presses back button)
        fragmentTransaction.addToBackStack("TradeFragment")
        fragmentTransaction.commit()
    }

    override fun onCoinsSelected(selectedCoins: ArrayList<Coin>?, selectionType: String?) {
        when (selectionType) {
            tradeSelection -> sendCoins(recipient, selectedCoins)
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onPause() {
        super.onPause()
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
        super.onBackPressed()
        // when user presses back button, fragment will be closed, so we need to set button that launched fragment to visible
        tradeButton.visibility = View.VISIBLE
    }
}