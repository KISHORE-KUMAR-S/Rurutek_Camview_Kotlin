package com.android.rurutekcamview

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer


class AddActivity : AppCompatActivity(), SurfaceHolder.Callback {
    private var libVLC: LibVLC? = null
    private var mediaPlayer: MediaPlayer? = null
    private var ipAddress: EditText? = null
    private var cameraName: EditText? = null
    private var username: EditText? = null
    private var password: EditText? = null
    private var streamingPath: EditText? = null
    private var saveButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        val surfaceView = findViewById<SurfaceView>(R.id.surfaceView)
        val testButton = findViewById<Button>(R.id.testbtn)
        ipAddress = findViewById(R.id.etIpAddress)
        cameraName = findViewById(R.id.etcamname)
        username = findViewById(R.id.etRtspUsername)
        password = findViewById(R.id.etRtspPassword)
        streamingPath = findViewById(R.id.etStreamingPath)
        saveButton = findViewById(R.id.savebtn)
        val back = findViewById<ImageButton>(R.id.back)
        libVLC = LibVLC(this)
        mediaPlayer = MediaPlayer(libVLC)
        surfaceView.holder.addCallback(this)
        setStatusBarTextColor()
        val intent = intent
        if (intent != null) {
            val ip = intent.getStringExtra("ipAddress")
            if (!ip.isNullOrBlank()) {
                ipAddress?.setText(ip)
            }
        }
        testButton.setOnClickListener {
            if (ipAddress?.text?.toString() != "" && streamingPath?.text?.toString() != "" && cameraName?.text?.toString() != "" && username?.text?.toString() != "" && password?.text?.toString() != "") {
                performTest()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Please fill all required fields",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        saveButton?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                saveCameraDetailsToDatabase()
                finish()
            }

            private fun saveCameraDetailsToDatabase() {
                val ipAddressValue = ipAddress?.text?.toString() ?: ""
                val streamingPathValue = streamingPath?.text?.toString() ?: ""
                val cameraNameValue = cameraName?.text?.toString() ?: ""
                val usernameValue = username?.text?.toString() ?: ""
                val passwordValue = password?.text?.toString() ?: ""

                val cameraDetails = CameraDetails()
                cameraDetails.setIpAddress(ipAddressValue)
                cameraDetails.setStreamingPath(streamingPathValue)
                cameraDetails.setCameraName(cameraNameValue)
                cameraDetails.setRtspUsername(usernameValue)
                cameraDetails.setRtspPassword(passwordValue)
                val dataSource = CameraDetailsDataSource(
                    applicationContext
                )
                dataSource.open()
                if (dataSource.isIpAddressExists(ipAddressValue)) {
                    Toast.makeText(
                        applicationContext,
                        "IP Address already exists",
                        Toast.LENGTH_SHORT
                    ).show()
                    dataSource.close()
                    return
                }
                val result: Long = dataSource.saveCameraDetails(cameraDetails)
                dataSource.close()
                if (result != -1L) {
                    Toast.makeText(applicationContext, "Saved", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Camera Name already exists",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
        back.setOnClickListener { finish() }
    }

    private fun performTest() {
        Thread {
            val rtspRequest =
                "rtsp://" + username!!.text.toString() + ":" + password!!.text
                    .toString() + "@" + ipAddress!!.text
                    .toString() + "/" + streamingPath!!.text.toString()
            val media =
                Media(libVLC, Uri.parse(rtspRequest))
            try {
                mediaPlayer!!.media = media
                mediaPlayer!!.play()
                runOnUiThread {
                    saveButton!!.isEnabled = true
                    saveButton!!.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.white
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "Error during media setup",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.start()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        mediaPlayer!!.vlcVout.setVideoSurface(holder.surface, null)
        mediaPlayer!!.vlcVout.attachViews()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        mediaPlayer!!.vlcVout.setWindowSize(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        mediaPlayer!!.vlcVout.detachViews()
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

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.stop()
        }
        mediaPlayer!!.release()
        libVLC!!.release()
    }
}