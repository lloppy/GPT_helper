package com.example.gpt.ui.settings

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.example.gpt.R

class PhrasesHelper (private val context: Context) {
    val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

    private var tempSet: MutableSet<String> = mutableSetOf()

    fun showCustomPhrasesAlert() {
        val alertDialogBuilder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.custom_phrases_alert, null)

        alertDialogBuilder.setView(dialogView)

        val alertDialog = alertDialogBuilder.create()

      
        alertDialog.show()
    }



}
