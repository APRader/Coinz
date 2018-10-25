package com.example.s1611382.ilp

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.coin_text_view.view.*

class WalletAdapter(private val myDataset: ArrayList<Coin>) :
        RecyclerView.Adapter<WalletAdapter.MyViewHolder>() {

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView = view.coin_id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletAdapter.MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.coin_text_view, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.textView.text = myDataset[position].toString()
    }

    override fun getItemCount() = myDataset.size
}