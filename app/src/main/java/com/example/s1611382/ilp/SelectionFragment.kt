package com.example.s1611382.ilp

import android.content.Context
import android.os.Bundle
import android.support.v4.app.ListFragment
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

class SelectionFragment : ListFragment() {
    private lateinit var coinList: ArrayList<Coin>
    private var selectedCoins: ArrayList<Coin> = arrayListOf()
    private var selectionType: String = ""
    private var buttonText: String = "Continue"

    private lateinit var listener: OnCoinsSelected

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        coinList = arguments!!.getParcelableArrayList("coinList")
        selectionType = arguments!!.getString("selectionKey")
        buttonText = arguments!!.getString("textKey")

        val bankListAdapter = ArrayAdapter<Coin>(
                activity,
                android.R.layout.simple_list_item_multiple_choice,
                coinList)

        listAdapter = bankListAdapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.fragment_selection, container, false)

        val coinsButton: Button = fragmentView.findViewById(R.id.convert_coins_id)
        coinsButton.text = buttonText
        coinsButton.setOnClickListener {
            val count = listView.count
            val checkedCoins: SparseBooleanArray = listView.checkedItemPositions
            for (i in 0 until count) {
                if (checkedCoins.get(i)) {
                    // all coins the user has checked will get converted when button is clicked
                    val coin = listView.getItemAtPosition(i) as Coin
                    selectedCoins.add(coin)
                }
            }
            // alert bank activity that the converting has been completed and pass new wallet/bank
            listener.onCoinsSelected(selectedCoins, selectionType)
        }

        return fragmentView
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        // initalise the listener reference
        if (context is OnCoinsSelected) {
            listener = context
        } else {
            throw ClassCastException(context.toString() + " must implement OnCoinsSelected.")
        }
    }

    // used to alert the bank activity that user has deposited coins
    interface OnCoinsSelected {
        fun onCoinsSelected(selectedCoins: ArrayList<Coin>, selectionType: String)
    }
}
