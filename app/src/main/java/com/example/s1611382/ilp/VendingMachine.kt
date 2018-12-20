package com.example.s1611382.ilp

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class VendingMachine : BaseActivity() {
    private var gold: Double? = 0.0
    // 30 collectibles, true if player has them
    private var collectibles: Array<Boolean> = Array(30) {false}
    private lateinit var vendingButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.vending_machine)
        setToolbar()

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

    override fun onStart() {
        super.onStart()
        val settings = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        gold = settings.getString(GOLD_KEY, "")?.toDoubleOrNull()
        if (gold == null) {
            gold = 0.0
        }
        val goldView: TextView = findViewById(R.id.gold_id)
        goldView.text = String.format(getString(R.string.gold), gold)

        collectibles = prefsToCollectiblesArray(COLLECTIBLES_KEY)

        populateCollectibles()
    }

    /**
     * saves colelctibles array and GOLD in shared preferences
     */
    override fun onPause() {
        super.onPause()

        collectiblesArrayToPrefs(collectibles, COLLECTIBLES_KEY)

        val settings = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putString(COUNTER_KEY, gold.toString())
        editor.apply()
    }
}
