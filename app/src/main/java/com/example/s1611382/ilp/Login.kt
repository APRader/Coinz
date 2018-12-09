package com.example.s1611382.ilp

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    companion object {
        const val RC_SIGN_IN = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser

        // launches map if user already logged in
        if (user != null) { openMap() }

        // if user is not logged in, start text and a login button are displayed
        setContentView(R.layout.login)
        val loginButton : Button = findViewById(R.id.login_button_id)
        loginButton.setOnClickListener { _ -> login() }
    }

    private fun openMap() {
        val mapIntent = Intent(this, Map::class.java)
        startActivity(mapIntent)
        finish()
    }

    /**
     * uses AuthUI for login, which takes care of
     * signing up a new user (using email address)
     * logging in an existing user
     * resetting a forgotten password
     * and more (e.g. translation, Google SmartLock integration, ...)
     */
    private fun login() {
        val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build())
        startActivityForResult(
                AuthUI
                        .getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN
        )
    }

    /**
     * called when login is finished
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                // login successful
                openMap()
            } else {
                // user did not login, so we stay at login screen
                Toast.makeText(this, getString(R.string.login_failed_message), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
