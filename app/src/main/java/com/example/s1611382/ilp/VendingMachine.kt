package com.example.s1611382.ilp

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class VendingMachine : BaseActivity() {
    private var gold: Double? = 0.0
    // 30 collectibles, true if player has them
    private var collectibles: Array<Boolean> = Array(30) {false}
    private lateinit var vendingButton: Button

    private var firestore: FirebaseFirestore? = null
    private var firebaseEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.vending_machine)
        setToolbar()

        firestore = firestoreSetup()
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        firebaseEmail = firebaseUser?.email.toString()

        vendingButton = findViewById(R.id.vending_button_id)
        vendingButton.setOnClickListener{_ -> buyCollectible() }
    }

    /**
     * draws the images of all collectibles the player owns
     */
    private fun populateCollectibles() {
        for (i in collectibles.indices) {
            if (collectibles[i]) {
                // imageViews go from 1 to 30
                drawCollectible(i + 1)
            }
        }
    }

    /**
     * draws a collectible specified by its number
     */
    private fun drawCollectible(collectible: Int) {
        val viewId = resources.getIdentifier("collectible$collectible", "id", packageName)
        val imageView: ImageView = findViewById(viewId)
        val resId = resources.getIdentifier("collectible$collectible", "drawable", packageName)
        imageView.setImageDrawable(getDrawable(resId))
    }

    /**
     * randomly selects a collectible for the player for GOLD
     */
    private fun buyCollectible() {
        val builder = AlertDialog.Builder(this)
        if (gold!! >= COLLECTIBLE_PRICE) {
            // generates a random int from 0 to 29
            val randomCollectible = (Math.random()*30).toInt()

            builder.setTitle(getString(R.string.collectible_title))
            val resId = resources.getIdentifier("collectible${randomCollectible + 1}", "drawable", packageName)
            builder.setIcon(getDrawable(resId))

            if (collectibles[randomCollectible]) {
                // player already owns this collectible
                builder.setMessage(getString(R.string.collectible_message_old))
            } else {
                builder.setMessage(getString(R.string.collectible_message_new))
                drawCollectible(randomCollectible + 1)
            }
            builder.show()

            collectibles[randomCollectible] = true
            gold = gold!! - COLLECTIBLE_PRICE
            val goldView: TextView = findViewById(R.id.gold_id)
            goldView.text = String.format(getString(R.string.gold), gold)
        } else {
            builder.setTitle(getString(R.string.poor_collectible_title))
            builder.setMessage(getString(R.string.poor_collectible_message))
            builder.show()
        }
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
     * restore shared preferences, set gold text view and start populating collectibles
     */
    override fun onStart() {
        super.onStart()
        gold = prefsToGold()
        collectibles = prefsToCollectiblesArray(COLLECTIBLES_KEY)

        val goldView: TextView = findViewById(R.id.gold_id)
        goldView.text = String.format(getString(R.string.gold), gold)

        populateCollectibles()
    }

    /**
     * saves values in shared preferences and firestore
     */
    override fun onPause() {
        super.onPause()

        collectiblesArrayToPrefs(collectibles, COLLECTIBLES_KEY)

        val settings = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putString(GOLD_KEY, gold.toString())
        editor.apply()

        uploadBooleanArray(COLLECTIBLES_KEY, collectibles)
        val document = firestore?.collection(COLLECTION_KEY)?.document(firebaseEmail)
        val data = HashMap<String, Any>()
        data[GOLD_KEY] = gold!!
        document?.set(data, SetOptions.merge())
    }
}
