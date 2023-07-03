package com.example.gpt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.gpt.databinding.ActivityRegistrationBinding

class RegistrationActivity : AppCompatActivity() {
    lateinit var binding: ActivityRegistrationBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()


        binding.signIn.setOnClickListener {
            startMainActivity()
        }

    }


    private fun startMainActivity() {
        //if (auth.currentUser != null){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}