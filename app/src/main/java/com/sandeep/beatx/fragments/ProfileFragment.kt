package com.sandeep.beatx.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import com.sandeep.beatx.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.tvTheme.setOnClickListener {
            showThemeDialog()
        }
        
        binding.tvLanguage.setOnClickListener {
            showLanguageDialog()
        }

        binding.tvLogout.setOnClickListener {
            Toast.makeText(context, "Logout Clicked", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showThemeDialog() {
        val themes = arrayOf("Light", "Dark", "System Default", "AMOLED Black")
        
        val prefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val isAmoled = prefs.getBoolean("theme_amoled", false)
        
        val currentThemeIndex = when {
            isAmoled -> 3
            AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO -> 0
            AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES -> 1
            else -> 2
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Choose Theme")
            .setSingleChoiceItems(themes, currentThemeIndex) { dialog, which ->
                when (which) {
                    0 -> {
                        prefs.edit().putBoolean("theme_amoled", false).apply()
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                    1 -> {
                        prefs.edit().putBoolean("theme_amoled", false).apply()
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                    2 -> {
                        prefs.edit().putBoolean("theme_amoled", false).apply()
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    }
                    3 -> {
                        prefs.edit().putBoolean("theme_amoled", true).apply()
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                }
                requireActivity().recreate()
                dialog.dismiss()
            }
            .show()
    }
    
    private fun showLanguageDialog() {
        val languages = arrayOf("English", "हिंदी (Hindi)")
        
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        val currentLanguageIndex = if (currentLocales.toLanguageTags().contains("hi")) 1 else 0

        AlertDialog.Builder(requireContext())
            .setTitle("Choose Language")
            .setSingleChoiceItems(languages, currentLanguageIndex) { dialog, which ->
                val locale = if (which == 1) "hi" else "en"
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(locale))
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
