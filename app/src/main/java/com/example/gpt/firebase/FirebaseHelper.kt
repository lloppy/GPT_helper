package com.example.gpt.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class FirebaseHelper {
    var role: Role = Role.USER

    var username: String
    var database: FirebaseDatabase
    var usersRef: DatabaseReference
    var usernameRef: DatabaseReference
    var roleRef: DatabaseReference


    private var auth: FirebaseAuth = Firebase.auth

    init {
        username = auth.currentUser!!.displayName.toString()
        database = FirebaseDatabase.getInstance()
        usersRef = database.getReference("users")
        usernameRef = usersRef.child(username)
        roleRef = usernameRef.child("role")

       // getUserRoleFromFirebase { role = it }
    }

    fun loadUser() {
        usersRef.child(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Если пользователя нет в базе данных, создаем новый узел для него
                    usersRef.child(username).setValue(Role.USER)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        Log.e("item", auth.currentUser?.displayName.toString())
    }

    fun getUserRoleFromFirebase(callback: (diet: Role) -> Unit) {
        roleRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val role = dataSnapshot.value as Role

                callback(role)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("item", "in getNutrientsFromFirebase Firebase onCancelled")
            }
        })
    }

    fun signOut() {
        auth.signOut()
    }
}