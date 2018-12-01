package com.example.s1611382.ilp

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase .firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.trading.*

class Trading : AppCompatActivity(){
    private val RC_SIGN_IN = 123
    private var firestore: FirebaseFirestore? = null
    private var users: CollectionReference? = null
    private var firestoreTrading: DocumentReference? = null

    companion object {
        private const val TAG = "Coinz"
        private const val COLLECTION_KEY = "Trading"
        private const val DOCUMENT_KEY = "Message"
        private const val NAME_FIELD = "Name"
        private const val COIN_FIELD = "Coin"
    }

    override fun onCreate(savedInstanceState: Bundle?   ) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        // check if user already logged in
        if (user == null) {
            // authentication using AuthUI
            login()
        }

        setContentView(R.layout.trading)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar: ActionBar? = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)

        firestore = FirebaseFirestore.getInstance()
        // Use com.google.firebase.Timestamp objects instead of java.util.Date objects
        val settings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build()
        firestore?.firestoreSettings = settings
        users = firestore?.collection(COLLECTION_KEY)
        //realtimeUpdateListener()

        trade_button_id.setOnClickListener{_ -> sendCoins() }
        logout_button_id.setOnClickListener{_ -> logout() }
    }

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

    private fun logout() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener {
                    finish()
                }
    }

    private fun sendCoins() {
        // user has to enter email of recipient
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Who do you want to send it to?")
        val view = layoutInflater.inflate(R.layout.trade_dialog, null)
        val editText = view.findViewById(R.id.trade_edit_id) as EditText
        builder.setView(view)

        builder.setPositiveButton(android.R.string.ok) {dialog, p1 ->
            val recipient = editText.text
            var isValid = true
            if (recipient.isBlank()) {
                dialog.dismiss()
            } else {
                //TODO: yeet
            }
        }

        builder.setNegativeButton(android.R.string.cancel) {dialog, p1 ->
            dialog.cancel()
        }

        builder.show()

        val user = FirebaseAuth.getInstance().currentUser
        val newTrade = mapOf(
                NAME_FIELD to "namefest",
                COIN_FIELD to "coinfest")
        users?.document("yeet")?.collection(user?.email!!)?.document("coins")?.set(newTrade)
        firestoreTrading?.set(newTrade)
                ?.addOnSuccessListener { print("Trade sent!") }
                ?.addOnFailureListener { e -> Log.e(TAG, e.message)}
    }

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

    override fun onStart() {
        super.onStart()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                //val user = FirebaseAuth.getInstance().currentUser
                //print(user)
            } else {
                // user did not login, so we go back to map
                Toast.makeText(this, "Login failed.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}