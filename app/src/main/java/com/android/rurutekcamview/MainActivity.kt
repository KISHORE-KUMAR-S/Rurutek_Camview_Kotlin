package com.android.rurutekcamview

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener{
    private val switchFragmentTag = SwitchFragment::class.java.name
    private val itemFragmentTag = ItemFragment::class.java.name
    private val settingsFragmentTag = SettingsFragment::class.java.name
    private var bottomNavigationView: BottomNavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setStatusBarTextColor()

        setContentView(R.layout.activity_main)
        bottomNavigationView = findViewById(R.id.bottom_nav)
        bottomNavigationView?.setOnItemSelectedListener(this)

        val iconColor = Color.WHITE
        val textColor = Color.WHITE

        setBottomNavColors(iconColor, textColor)

        bottomNavigationView?.itemActiveIndicatorColor = ColorStateList.valueOf(
            ContextCompat.getColor(
                this,
                R.color.black
            )
        )

        if (savedInstanceState == null) {
            // Store the currently displayed fragment
            loadFragment(SwitchFragment(), switchFragmentTag)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var fragment: Fragment?
        val itemId = item.itemId
        if (itemId == R.id.add_cam) {
            fragment = supportFragmentManager.findFragmentByTag(switchFragmentTag)
            if (fragment == null) {
                fragment = SwitchFragment()
                loadFragment(fragment, switchFragmentTag)
            }
        } else if (itemId == R.id.list) {
            fragment = supportFragmentManager.findFragmentByTag(itemFragmentTag)
            if (fragment == null) {
                fragment = ItemFragment()
                loadFragment(fragment, itemFragmentTag)
            }
        } else if (itemId == R.id.settings) {
            fragment = supportFragmentManager.findFragmentByTag(settingsFragmentTag)
            if (fragment == null) {
                fragment = SettingsFragment()
                loadFragment(fragment, settingsFragmentTag)
            }
        } else {
            fragment = null
        }
        return fragment != null
    }

    private fun loadFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction().replace(R.id.relativeLayout, fragment, tag)
            .commit()
    }

    private fun setStatusBarTextColor() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.statusBarColor = ContextCompat.getColor(this, R.color.transparent)
        }
        window.navigationBarColor = Color.TRANSPARENT
    }

    private fun setBottomNavColors(iconColor: Int, textColor: Int) {
        val iconColors = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ), intArrayOf(iconColor, textColor)
        )
        bottomNavigationView!!.itemIconTintList = iconColors
        bottomNavigationView!!.itemTextColor = iconColors
    }
}