package com.example.s1611382.ilp

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class Trading : AppCompatActivity(){
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        mAuth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()
        // check if user is signed in (non-null) and update UI
        updateUI(mAuth?.currentUser)
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        //TODO
    }

    fun createAccount(email: String, password: String) {
        mAuth?.createUserWithEmailAndPassword(email, password)
                ?.addOnCompleteListener(this) {task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with user info
                } else {
                    // sign in failed, display a message to the user
                }
            }
    }
}