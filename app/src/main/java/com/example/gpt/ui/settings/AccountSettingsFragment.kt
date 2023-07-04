package com.example.gpt.ui.settings

import KeysHelper
import TemplateHelper
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.gpt.R
import com.example.gpt.RegistrationActivity
import com.example.gpt.databinding.FragmentAccountSettingsBinding
import com.example.gpt.firebase.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth

class AccountSettingsFragment : Fragment() {
    lateinit var binding: FragmentAccountSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpUserPicture(binding.avatar, binding.tvUsername)

        initCustomSettings()
        buttonSignOut()
    }

    private fun initCustomSettings() {
        binding.keys.setOnClickListener {
            KeysHelper(requireContext()).showCustomKeysAlert()
        }

        binding.temp.setOnClickListener {
            TemplateHelper(requireContext()).showCustomTempAlert()
        }

        binding.saved.setOnClickListener {
            PhrasesHelper(requireContext()).showCustomPhrasesAlert()
        }
    }

    private fun setUpUserPicture(imageView: ImageView, userName: TextView) {
        Glide.with(this).load(FirebaseAuth.getInstance().currentUser?.photoUrl)
            .transform(RoundedCorners(300)).into(imageView)
        userName.text = FirebaseAuth.getInstance().currentUser!!.displayName
    }


    private fun buttonSignOut() {
        binding.buttonSignOut.setOnClickListener {
            val firebaseHelper = FirebaseHelper()
            firebaseHelper.signOut()
            val intent = Intent(activity, RegistrationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            requireContext().getSharedPreferences("MY_APP", Context.MODE_PRIVATE).edit()
                .putBoolean("IS_LOGGED_IN", false).apply()
            startActivity(intent)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = AccountSettingsFragment()
    }
}