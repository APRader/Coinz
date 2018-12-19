package com.example.s1611382.ilp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem

class VendingMachine : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vending_machine)
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
}
