package com.example.s1611382.ilp

import android.R.attr.fragment
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.ListFragment
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.R.attr.prompt
import android.widget.*

class BankFragment : ListFragment() {
    private lateinit var coinBank: ArrayList<Coin>
    private var convertedCoins: ArrayList<Coin> = arrayListOf()

    private lateinit var listener: OnCoinsConverted

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        coinBank = arguments!!.getParcelableArrayList(Bank.COINBANK)

        val bankListAdapter = ArrayAdapter<Coin>(
                activity,
                android.R.layout.simple_list_item_multiple_choice,
                coinBank)

        listAdapter = bankListAdapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.fragment_bank, container, false)

        val convertCoinsButton: Button = fragmentView.findViewById(R.id.convert_coins_id)
        convertCoinsButton.setOnClickListener {
            val count = listView.count
            val checkedCoins: SparseBooleanArray = listView.checkedItemPositions
            for (i in 0 until count) {
                if (checkedCoins.get(i)) {
                    // all coins the user has checked will get converted when button is clicked
                    val coin = listView.getItemAtPosition(i) as Coin
                    convertedCoins.add(coin)
                }
            }
            // alert bank activity that the converting has been completed and pass new wallet/bank
            listener.onCoinsConverted(convertedCoins)
        }

        return fragmentView
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        // initalise the listener reference
        if (context is OnCoinsConverted) {
            listener = context
        } else {
            throw ClassCastException(context.toString() + " must implement OnCoinsConverted.")
        }
    }

    // used to alert the bank activity that user has deposited coins
    interface OnCoinsConverted {
        fun onCoinsConverted(convertedCoins: ArrayList<Coin>)
    }
}
