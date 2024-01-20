package com.android.rurutekcamview

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ExpandableListView
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.util.*

class LiveGridShowActivity : AppCompatActivity(), CameraDataChangeListener {
    private lateinit var devicesSaved: TextView
    private lateinit var adapter: CameraSingleAdapter
    private val cameraList = ArrayList<CameraInfo>()
    private val editRequestCode = 1
    private var lastExpandableGroup = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_grid_show)

        val expandableListView: ExpandableListView = findViewById(R.id.expandableListView)
        devicesSaved = findViewById(R.id.devicesSavedTV)

        val back: ImageButton = findViewById(R.id.back)
        val switcher: ImageButton = findViewById(R.id.switcher)

        back.setOnClickListener { finish() }

        switchToLiveFragment()

        switcher.setOnClickListener {
            if (!isLiveFragment()) {
                switchToLiveFragment()
            }
        }

        setStatusBarTextColor()

        FetchDataFromDatabase().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

        adapter = CameraSingleAdapter(this, cameraList, this)
        expandableListView.setAdapter(adapter)

        expandableListView.setOnGroupClickListener { _, _, groupPosition, _ ->
            lastExpandableGroup = if (expandableListView.isGroupExpanded(groupPosition)) {
                expandableListView.collapseGroup(groupPosition)
                -1
            } else {
                if (lastExpandableGroup != -1) {
                    expandableListView.collapseGroup(lastExpandableGroup)
                }
                expandableListView.expandGroup(groupPosition)
                groupPosition
            }
            true
        }

        if (savedInstanceState == null || isLiveFragment()) {
            switchToLiveFragment()
        }
    }

    private fun isLiveFragment(): Boolean {
        val currentFragment: Fragment? = supportFragmentManager.findFragmentById(R.id.frameLayout)
        return currentFragment is LiveFragment
    }

    private fun switchToLiveFragment() {
        if (!isFinishing && !isDestroyed) {
            var liveFragment: LiveFragment? = supportFragmentManager.findFragmentByTag(LiveFragment::class.java.simpleName) as LiveFragment?
            if (liveFragment == null) {
                liveFragment = LiveFragment()
            }
            supportFragmentManager.beginTransaction().replace(R.id.frameLayout, liveFragment).commit()
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun setStatusBarTextColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isStatusBarContrastEnforced = false
                window.statusBarColor = ContextCompat.getColor(this, R.color.transparent)
            }
            window.navigationBarColor = Color.TRANSPARENT
        }
    }

    private fun fetchCameraData() {
        FetchDataFromDatabase().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class FetchDataFromDatabase : AsyncTask<Void?, Void?, ArrayList<CameraInfo>>() {
        override fun doInBackground(vararg voids: Void?): ArrayList<CameraInfo> {
            val cameraList = ArrayList<CameraInfo>()
            try {
                CameraDetailsDbHelper(this@LiveGridShowActivity).use { dbHelper ->
                    dbHelper.allCameraDetails.use { cursor ->
                        while (cursor.moveToNext()) {
                            val ipAddress = cursor.getString(cursor.getColumnIndexOrThrow(CameraDetailsDbHelper.IP_ADDRESS))
                            val streamingPath = cursor.getString(cursor.getColumnIndexOrThrow(CameraDetailsDbHelper.STREAMING_PATH))
                            val cameraName = cursor.getString(cursor.getColumnIndexOrThrow(CameraDetailsDbHelper.CAMERA_NAME))
                            val rtspUsername = cursor.getString(cursor.getColumnIndexOrThrow(CameraDetailsDbHelper.RTSP_USERNAME))
                            val rtspPassword = cursor.getString(cursor.getColumnIndexOrThrow(CameraDetailsDbHelper.RTSP_PASSWORD))

                            cameraList.add(CameraInfo(cameraName, ipAddress, streamingPath, rtspUsername, rtspPassword))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return cameraList
        }

        @SuppressLint("SetTextI18n")
        override fun onPostExecute(result: ArrayList<CameraInfo>) {
            cameraList.clear()
            cameraList.addAll(result)

            val totalDevices = cameraList.size

            devicesSaved.text = "Devices Saved: $totalDevices"
            adapter.notifyDataSetChanged()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == editRequestCode && resultCode == RESULT_OK) {
            data?.let {
                val updatedCameraInfo: CameraInfo = it.getParcelableExtra("updatedCameraInfo")!!
                updatedCameraInfoInList(updatedCameraInfo)
            }
        }
    }

    private fun updatedCameraInfoInList(updatedCameraInfo: CameraInfo) {
        val position = findCameraInfoPosition(updatedCameraInfo)
        if (position != -1) {
            cameraList[position] = updatedCameraInfo
            adapter.notifyDataSetChanged()
        }
    }

    private fun findCameraInfoPosition(updatedCameraInfo: CameraInfo): Int {
        val targetIpAddress = updatedCameraInfo.ipAddress
        for (i in cameraList.indices) {
            val currentCamera: CameraInfo = cameraList[i]
            if (targetIpAddress == currentCamera.ipAddress) {
                return i
            }
        }
        return -1
    }

    override fun onCameraDataChanged() {
        fetchCameraData()
    }

    fun updateLiveFragment() {
        switchToLiveFragment()
    }
}