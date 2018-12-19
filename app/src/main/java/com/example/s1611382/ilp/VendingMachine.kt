package com.example.s1611382.ilp

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView

class VendingMachine : BaseActivity() {
    private var gold: Double? = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.vending_machine)
        setToolbar()
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
    }
}
