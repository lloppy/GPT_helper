package com.example.gpt.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class FirebaseHelper {
    var username: String
    var database: FirebaseDatabase
    var usersRef: DatabaseReference
    var usernameRef: DatabaseReference

    private var auth: FirebaseAuth = Firebase.auth

    init {
        username = auth.currentUser!!.displayName.toString()
        database = FirebaseDatabase.getInstance()
        usersRef = database.getReference("users")
        usernameRef = usersRef.child(username)
        //getUserDietFromFirebase { diet = it }
    }

    fun loadUser() {
        usersRef.child(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Если пользователя нет в базе данных, создаем новый узел для него
                    usersRef.child(username).setValue("")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        Log.e("item", auth.currentUser?.displayName.toString())
    }

    fun signOut() {
        auth.signOut()
    }
}