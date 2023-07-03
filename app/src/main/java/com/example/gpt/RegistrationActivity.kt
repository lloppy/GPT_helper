package com.example.gpt

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.startActivity
import com.example.gpt.databinding.ActivityRegistrationBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RegistrationActivity : AppCompatActivity() {
    lateinit var binding: ActivityRegistrationBinding
    lateinit var launcher: ActivityResultLauncher<Intent>
    lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val isLoggedIn = getSharedPreferences("MY_APP", Context.MODE_PRIVATE)
            .getBoolean("IS_LOGGED_IN", false)

        if (isLoggedIn) {
            startMainActivity()
        }

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            auth = Firebase.auth
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    fbAuthWithGoogle(account.idToken!!)
                }
            } catch (e: ApiException) {
                Log.d("MyLog", "Api Exeption")
            }
        }

        binding.signIn.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun getClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(this, gso)
    }

    private fun signInWithGoogle() {
        val sigInClient = getClient()
        launcher.launch(sigInClient.signInIntent)
    }

    private fun fbAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("MyLog", "Google sign in done")
                // После успешной авторизации сохраняем информацию в SharedPreferences
                getSharedPreferences("MY_APP", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("IS_LOGGED_IN", true)
                    .apply()

                startMainActivity()
            } else {
                Log.d("MyLog", "Google sign in error")
            }
        }
    }

    private fun startMainActivity() {
        //if (auth.currentUser != null){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
