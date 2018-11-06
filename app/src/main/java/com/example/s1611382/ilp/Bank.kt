package com.example.s1611382.ilp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.coin_text_view.view.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class Bank: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bank)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar: ActionBar? = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)

        val rates: ArrayList<Coin> = intent.extras.getParcelableArrayList(MainActivity.RATES)

        var textView: TextView = findViewById(R.id.rates_id)
        textView.text = rates.toString()

    }

}
