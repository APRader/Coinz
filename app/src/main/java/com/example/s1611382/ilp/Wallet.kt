package com.example.s1611382.ilp

import android.app.Activity
import android.app.ListActivity
import android.app.LoaderManager
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatViewInflater
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.ThemedSpinnerAdapter
import kotlinx.android.synthetic.main.wallet.view.*

class Wallet : Activity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager


     //arrayListOf("this", "is", "definitely", "not", "going", "to", "work",
            //"at", "all", ":(", "yeet!", "Get", "spooked")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wallet)

        val myDataset: ArrayList<String> = intent.extras.getStringArrayList(MainActivity.COINS)

        viewManager = LinearLayoutManager(this)
        viewAdapter = WalletAdapter(myDataset)

        recyclerView = findViewById<RecyclerView>(R.id.wallet).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

    }
}