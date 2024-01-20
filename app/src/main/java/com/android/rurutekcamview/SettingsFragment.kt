package com.android.rurutekcamview

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val toggleSwitch = view.findViewById<MaterialSwitch>(R.id.toggleSwitch)
        val aboutBtn = view.findViewById<Button>(R.id.about)

        toggleSwitch.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            val context = buttonView.context
            if (isChecked) {
                // Set color when the toggle button is checked
                val colorChecked = ContextCompat.getColor(context, R.color.black)
                buttonView.setBackgroundColor(colorChecked)
            } else {
                // Set color when the toggle button is unchecked
                val colorUnchecked = ContextCompat.getColor(context, R.color.button)
                buttonView.setBackgroundColor(colorUnchecked)
            }
        }

        val preferences = requireContext().getSharedPreferences(
            requireContext().packageName + "_preferences",
            Context.MODE_PRIVATE
        )
        val shouldShowLogin = preferences.getBoolean("show_login", false)
        toggleSwitch.isChecked = shouldShowLogin

        toggleSwitch.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                if (isDeviceSecure()) {
                    val editor = preferences.edit()
                    editor.putBoolean("show_login", true)
                    editor.apply()
                } else {
                    showSecurityDialog()
                    toggleSwitch.isChecked = false
                }
            } else {
                val editor = preferences.edit()
                editor.putBoolean("show_login", false)
                editor.apply()
            }
        }

        aboutBtn.setOnClickListener {
            val intent = Intent(
                requireContext(),
                AboutActivity::class.java
            )
            startActivity(intent)
        }

        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showExitConfirmationDialog()
                }
            })

        return view
    }
    private fun showSecurityDialog() {
        MaterialAlertDialogBuilder(requireContext()).setTitle("Security Required")
            .setMessage("To enable this feature, you need to set a pattern, PIN or password for your device.")
            .setPositiveButton("OK", null).show()
    }

    private fun isDeviceSecure(): Boolean {
        val keyguardManager =
            requireContext().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return keyguardManager.isDeviceSecure
    }

    private fun showExitConfirmationDialog() {
        val dialogFragment = ExitConfirmationDialogFragment()
        dialogFragment.show(childFragmentManager, "ExitConfirmationDialogFragment")
    }
}