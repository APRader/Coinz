package com.example.s1611382.ilp

import android.content.Context
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth

class TestConditions {
    fun signOutUser(context: Context) {
        AuthUI.getInstance().signOut(context)
    }
}