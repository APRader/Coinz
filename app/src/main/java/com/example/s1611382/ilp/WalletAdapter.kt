package com.example.s1611382.ilp

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.coin_text_view.view.*

class WalletAdapter(private val myDataset: ArrayList<Coin>) :
        RecyclerView.Adapter<WalletAdapter.MyViewHolder>() {

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView = view.coin_id!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletAdapter.MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.coin_text_view, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.textView.text = myDataset[position].toString()
        var drawable = R.drawable.ic_copyright_black_24dp
        when (myDataset[position].currency) {
            "SHIL" -> drawable = R.drawable.coin_blue
            "DOLR" -> drawable = R.drawable.coin_green
            "QUID" -> drawable = R.drawable.coin_yellow
            "PENY" -> drawable = R.drawable.coin_red
        }
        holder.textView.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0)
    }

    override fun getItemCount() = myDataset.size
}