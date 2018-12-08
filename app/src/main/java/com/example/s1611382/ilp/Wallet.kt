package com.example.s1611382.ilp

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar

class Wallet : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wallet)

        //need toolbar for app nav drawer button

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar: ActionBar? = supportActionBar
        // creates up navigation to get back to parent activity (main map)
        actionbar?.setDisplayHomeAsUpEnabled(true)

        val coinWallet: ArrayList<Coin> = intent.extras.getParcelableArrayList(Map.COINWALLET)

        viewManager = LinearLayoutManager(this)
        viewAdapter = WalletAdapter(coinWallet)

        recyclerView = findViewById<RecyclerView>(R.id.wallet).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

    }

}