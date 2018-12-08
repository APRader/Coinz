package com.example.s1611382.ilp

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {
    private val RC_SIGN_IN = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        // check if user already logged in
        if (user != null) {
            // user is already logged in
            val mapIntent = Intent(this, Map::class.java)
            startActivity(mapIntent)
            finish()
        }

        setContentView(R.layout.login)
        var loginButton : Button = findViewById(R.id.login_button_id)
        loginButton.setOnClickListener { _ -> login() }

    }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                val mapIntent = Intent(this, Map::class.java)
                startActivity(mapIntent)
                finish()
            } else {
                // user did not login, so we stay at login screen
                Toast.makeText(this, "Login failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
