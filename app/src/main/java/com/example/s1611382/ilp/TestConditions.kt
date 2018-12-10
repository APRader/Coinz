package com.example.s1611382.ilp

import com.google.firebase.auth.FirebaseAuth

class TestConditions {
    fun signOutUser() {
        FirebaseAuth.getInstance().signOut()
    }
}