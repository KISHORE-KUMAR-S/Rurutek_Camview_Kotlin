package com.android.rurutekcamview

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat

class AuthenticationActivity : AppCompatActivity() {
    private val requestCode = 101011
    private var biometricPrompt: BiometricPrompt? = null
    private var promptInfo: PromptInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        setStatusBarTextColor()

        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS, BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED, BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED, BiometricManager.BIOMETRIC_STATUS_UNKNOWN, BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE, BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {}
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Prompts the user to create credentials that your app accepts.
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL)
                enrollIntent.putExtra(
                    Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                startActivityForResult(enrollIntent, requestCode, null)
            }
        }

        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this@AuthenticationActivity,
            executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        applicationContext,
                        "Authentication error: $errString",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    val intent = Intent(
                        this@AuthenticationActivity,
                        MainActivity::class.java
                    )
                    startActivity(intent)
                    finish() // Finish the AuthenticationActivity to prevent going back to it
                    Toast.makeText(
                        applicationContext,
                        "Authentication succeeded!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT)
                        .show()
                }
            })

        promptInfo = PromptInfo.Builder()
            .setTitle("Ruru Cam View")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText("Cancel")
            .build()

        startBiometricAuthentication() // Call startBiometricAuthentication() when you want to initiate the biometric authentication.

    }


    private fun setStatusBarTextColor() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isNightMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        if (isNightMode) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isStatusBarContrastEnforced = false
                window.statusBarColor =
                    ContextCompat.getColor(this, android.R.color.background_dark)
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isStatusBarContrastEnforced = false
                window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            }
        }
        window.navigationBarColor = Color.TRANSPARENT
    }

    private fun startBiometricAuthentication() {
        runOnUiThread { biometricPrompt!!.authenticate(promptInfo!!) }
    }
}