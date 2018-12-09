package com.example.s1611382.ilp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import timber.log.Timber

abstract class BaseActivity : AppCompatActivity() {

    companion object {
        const val COIN_WALLET = "coinWallet"
        const val RATES = "rates"
        const val COLLECTION_KEY = "Users"
        const val WALLET_KEY = "Wallet"
        const val PREF_FILE = "MyPrefsFile"
    }

    open fun setToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar: ActionBar? = supportActionBar
        // creates up navigation to get back to parent activity (main map)
        actionbar?.setDisplayHomeAsUpEnabled(true)
        }

    fun firestoreSetup(): FirebaseFirestore? {
        val firestore = FirebaseFirestore.getInstance()
        // Use com.google.firebase.Timestamp objects instead of java.util.Date objects
        val firebaseSettings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build()
        firestore.firestoreSettings = firebaseSettings
        return firestore
    }

    /**
     * update Firestore with contents of local wallet
     */
    fun uploadWallet(collection: CollectionReference?, wallet: ArrayList<Coin>) {
        collection?.get()
                ?.addOnSuccessListener { documents ->
                    // delete all coins in firebase,
                    for (document in documents) {
                        document.reference.delete()
                                .addOnSuccessListener { Timber.d("deleted coin $document") }
                                .addOnFailureListener{e -> Timber.e(e.message) }
                    }
                    // add current coins into firebase
                    for (coin in wallet) {
                        collection
                                .add(coin)
                                .addOnSuccessListener { Timber.d("Uploaded coin $coin") }
                                .addOnFailureListener { e -> Timber.e(e.message) }
                    }
                }
                ?.addOnFailureListener {e -> Timber.e(e.message)
                }
    }
}

