package com.android.rurutekcamview

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        setStatusBarTextColor()

        val back = findViewById<ImageButton>(R.id.back)
        back.setOnClickListener { finish() }
    }

    private fun setStatusBarTextColor() {
        setSystemUiVisibility()
        setStatusBarColor()
        setNavigationBarColor()
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun setSystemUiVisibility() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.statusBarColor = ContextCompat.getColor(this, R.color.transparent)
        }
    }

    private fun setNavigationBarColor() {
        window.navigationBarColor = Color.TRANSPARENT
    }
}