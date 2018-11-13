package com.example.s1611382.ilp

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.ListFragment
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.R.attr.prompt
import android.widget.ListAdapter
import android.widget.Toast

class WalletFragment : ListFragment() {
    private lateinit var coinWallet: ArrayList<Coin>

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
        return inflater.inflate(R.layout.fragment_wallet, container, false)
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)

        var prompt = "clicked item " + listView.getItemAtPosition(position).toString() + "\n"
        prompt += "selected items: \n"
        /*val count = listView.count
        val sparseBooleanArray : SparseBooleanArray = listView.checkedItemPositions
        for (i in 0 until count) {
            if (sparseBooleanArray.get(i)) {
                prompt += (listView.getItemAtPosition(i).toString() + "\n")
            }
        }*/
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     *
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment WalletFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                WalletFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }*/
}
