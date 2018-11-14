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

class WalletFragment : ListFragment() {
    private lateinit var coinWallet: ArrayList<Coin>
    private var depositedCoins: ArrayList<Coin> = arrayListOf()

    private lateinit var listener: OnCoinsDeposited

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        coinWallet = arguments!!.getParcelableArrayList(Bank.COINWALLET)

        val walletListAdapter = ArrayAdapter<Coin>(
                activity,
                android.R.layout.simple_list_item_multiple_choice,
                coinWallet)

        listAdapter = walletListAdapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.fragment_wallet, container, false)

        val depositCoinsButton: Button = fragmentView.findViewById(R.id.deposit_coins_id)
        depositCoinsButton.setOnClickListener {
            val count = listView.count
            val checkedCoins: SparseBooleanArray = listView.checkedItemPositions
            for (i in 0 until count) {
                if (checkedCoins.get(i)) {
                    // all coins the user has checked will get converted when button is clicked
                    val coin = listView.getItemAtPosition(i) as Coin
                    depositedCoins.add(coin)
                }
            }
            // alert bank activity that the depositing has benn completed and pass new wallet/bank
            listener.onCoinsDeposited(depositedCoins)
        }

        return fragmentView
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        // initalise the listener reference
        if (context is OnCoinsDeposited) {
            listener = context
        } else {
            throw ClassCastException(context.toString() + " must implement OnCoinsDeposited.")
        }
    }

    // used to alert the bank activity that user has deposited coins
    interface OnCoinsDeposited {
        fun onCoinsDeposited(depositedCoins: ArrayList<Coin>)
    }
}
