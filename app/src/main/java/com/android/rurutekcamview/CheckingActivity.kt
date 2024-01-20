package com.android.rurutekcamview

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity

class CheckingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checking)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)

        val showLoginKey = "show_login"

        val shouldShowLogin = preferences.getBoolean(showLoginKey, false)

        val targetActivityClass: Class<*> =
            if (shouldShowLogin) AuthenticationActivity::class.java else MainActivity::class.java

        startActivity(Intent(this@CheckingActivity, targetActivityClass))

        finish()
    }
}