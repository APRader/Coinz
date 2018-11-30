package com.example.s1611382.ilp

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase .firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.trading.*

class Trading : AppCompatActivity(){
    private val RC_SIGN_IN = 123
    private var firestore: FirebaseFirestore? = null
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

        val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build())
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN
        )

        //trade_button_id.setOnClickListener{_ -> sendCoins() }


    }

    override fun onStart() {
        super.onStart()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
        } else {
            print("yeet")
        }
    }
}