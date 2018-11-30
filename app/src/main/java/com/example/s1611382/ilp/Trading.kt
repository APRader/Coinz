package com.example.s1611382.ilp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
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
        setContentView(R.layout.trading)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar: ActionBar? = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // authentication using AuthUI
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

        trade_button_id.setOnClickListener{_ -> sendCoins() }
        firestore = FirebaseFirestore.getInstance()
        // Use com.google.firebase.Timestamp objects instead of java.util.Date objects
        val settings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build()
        firestore?.firestoreSettings = settings
        users = firestore?.collection(COLLECTION_KEY)
        //realtimeUpdateListener()
    }

    private fun sendCoins() {
        val newTrade = mapOf(
                NAME_FIELD to "namefest",
                COIN_FIELD to "coinfest")
        users?.document("yeet")?.set(newTrade)
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
                val user = FirebaseAuth.getInstance().currentUser
                print(user)
            } else {
                val user = FirebaseAuth.getInstance().currentUser
                print(user)
            }
        }
    }
}