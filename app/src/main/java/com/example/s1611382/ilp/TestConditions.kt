package com.example.s1611382.ilp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class TestConditions {
    companion object {
        private const val EMAIL_EMPTY = "empty@test.com"
        private const val EMAIL_TEST = "test@email.com"
        private const val PASSWORD = "password"
    }

    private fun firestoreSetup(): FirebaseFirestore? {
        val firestore = FirebaseFirestore.getInstance()
        val firebaseSettings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build()
        firestore.firestoreSettings = firebaseSettings
        return firestore
    }

    fun signOutUser() {
        FirebaseAuth.getInstance().signOut()
    }

    /**
     * returns email of a user that doesn't have a firestore document
     */
    fun emptyUser(): String {
        val firestore = firestoreSetup()
        firestore?.collection("Users")?.document(EMAIL_EMPTY)?.delete()
        return EMAIL_EMPTY
    }

    fun testUser(): String {
        return EMAIL_TEST
    }

    fun getPassword(): String {
        return PASSWORD
    }

}
