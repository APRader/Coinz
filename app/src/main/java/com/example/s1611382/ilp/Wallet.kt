package com.example.s1611382.ilp

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem

class Wallet : BaseActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    /**
     * uses WalletAdapter to draw a list of coins in user's wallet
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wallet)
        setToolbar()

        var coinWallet: ArrayList<Coin>? = intent?.extras?.getParcelableArrayList(COIN_WALLET)
        if (coinWallet == null) {
            coinWallet = arrayListOf()
        }

        viewManager = LinearLayoutManager(this)
        viewAdapter = WalletAdapter(coinWallet)

        recyclerView = findViewById<RecyclerView>(R.id.wallet).apply {
            layoutManager = viewManager
            adapter = viewAdapter
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

}