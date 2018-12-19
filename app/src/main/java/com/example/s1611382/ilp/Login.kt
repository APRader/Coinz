package com.example.s1611382.ilp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

class Login : BaseActivity() {
    private var firestore: FirebaseFirestore? = null
    private var user: FirebaseUser? = null
    private lateinit var email: String
    private lateinit var loginButton: Button

    companion object {
        private const val RC_SIGN_IN = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        user = FirebaseAuth.getInstance().currentUser

        // launches map if user already logged in
        if (user != null) { downloadCoins() }
        else {
            // if user is not logged in, start text and a login button are displayed
            setContentView(R.layout.login)
            loginButton = findViewById(R.id.login_button_id)
            loginButton.setOnClickListener { _ -> login() }
        }
    }

    /**
     * informs user that data is downloading
     */
    private fun downloadCoins() {
        Toast.makeText(this, getString(R.string.download), Toast.LENGTH_SHORT).show()
        firestoreDownload()
    }

    /**
     * downloads fields from firebase and stores them in shared preferences
     */
    private fun firestoreDownload() {
        firestore = firestoreSetup()

        user = FirebaseAuth.getInstance().currentUser
        email = user?.email.toString()

        firestore?.collection(COLLECTION_KEY)
                ?.document(email)
                ?.get()
                ?.addOnSuccessListener { document ->
                    val gold = document.data?.get(GOLD_KEY) as Double?
                    val depositCounter = document.data?.get(COUNTER_KEY)
                    val downloadDate = document.data?.get(DOWNLOAD_KEY)
                    val timerStarted = document.data?.get(TIMER_KEY)
                    var walletString = document.data?.get(WALLET_KEY).toString()
                    var bankString = document.data?.get(BANK_KEY).toString()
                    var collectedString = document.data?.get(COLLECTED_KEY).toString()

                    walletString = nullList(walletString)
                    bankString = nullList(bankString)
                    collectedString = nullList(collectedString)

                    val settings = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
                    val editor = settings.edit()
                    editor.putString(GOLD_KEY, gold.toString())
                    editor.putString(COUNTER_KEY, depositCounter.toString())
                    editor.putString(DOWNLOAD_KEY, downloadDate.toString())
                    editor.putString(TIMER_KEY, timerStarted.toString())
                    editor.putString(WALLET_KEY, walletString)
                    editor.putString(BANK_KEY, bankString)
                    editor.putString(COLLECTED_KEY, collectedString)
                    editor.apply()

                    openMap()
                }
                ?.addOnFailureListener {e ->
                    Timber.e(e.message)
                }
    }

    private fun openMap() {

        val mapIntent = Intent(this, Map::class.java)
        startActivity(mapIntent)
        finish()
    }

    /**
     * uses AuthUI for login, which takes care of
     * signing up a new user (using email address)
     * logging in an existing user
     * resetting a forgotten password
     * and more (e.g. translation, Google SmartLock integration, ...)
     */
    private fun login() {
        val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build())
        startActivityForResult(
                AuthUI
                        .getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN
        )
    }

    /**
     * called when login is finished
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                // login successful
                loginButton.visibility = View.GONE
                downloadCoins()
            } else {
                // user did not login, so we stay at login screen
                Toast.makeText(this, getString(R.string.login_failed_message), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
