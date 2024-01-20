package com.android.rurutekcamview

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import java.lang.ref.WeakReference
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class ViewsActivity : AppCompatActivity(), TextureView.SurfaceTextureListener,
    SurfaceHolder.Callback {
    private lateinit var libVLC: LibVLC
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var surfaceView: SurfaceView
    private lateinit var textureView: TextureView
    private var rtspUrl: String = ""

    private val executor: Executor = Executors.newFixedThreadPool(2)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_views)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        surfaceView = findViewById(R.id.surfaceView)
        textureView = findViewById(R.id.textureView)

        val surfaceHolder: SurfaceHolder = surfaceView.holder
        surfaceHolder.addCallback(this)

        textureView.surfaceTextureListener = this

        setStatusBarTextColor()

        val back: ImageButton = findViewById(R.id.back)
        back.setOnClickListener { finish() }

        val cameraViewModel = ViewModelProvider(this)[CameraViewModel::class.java]
        val cameraInfo = intent.getParcelableExtra<CameraInfo>("cameraInfo")

        if (cameraInfo != null) {
            cameraViewModel.setCameraInfo(cameraInfo)
            rtspUrl =
                "rtsp://${cameraInfo.rtspUsername}:${cameraInfo.rtspPassword}@${cameraInfo.ipAddress}/${cameraInfo.streamingPath}"
        }

        MediaInitializationTask(this).executeOnExecutor(executor)
    }
    private class MediaInitializationTask(activity: ViewsActivity) :
        AsyncTask<Void?, Void?, Void?>() {
        private val activityWeakReference: WeakReference<ViewsActivity> =
            WeakReference(activity)

        override fun doInBackground(vararg voids: Void?): Void? {
            val activity = activityWeakReference.get()
            if (activity != null) {
                activity.libVLC = LibVLC(activity)
                activity.mediaPlayer = MediaPlayer(activity.libVLC)
                val media = Media(activity.libVLC, Uri.parse(activity.rtspUrl))

                activity.mediaPlayer.media = media
                activity.mediaPlayer.play()
                activity.mediaPlayer.volume = 0
            }
            return null
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun setStatusBarTextColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.statusBarColor = ContextCompat.getColor(this, R.color.transparent)
            }
            window.navigationBarColor = Color.TRANSPARENT
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                textureView.visibility = View.GONE
                surfaceView.visibility = View.VISIBLE
            }
            Configuration.ORIENTATION_PORTRAIT -> {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                surfaceView.visibility = View.GONE
                textureView.visibility = View.VISIBLE
            }
            else -> {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        mediaPlayer.vlcVout.setVideoSurface(surfaceHolder.surface, surfaceHolder)
        mediaPlayer.vlcVout.attachViews()
    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, format: Int, width: Int, height: Int) {
        mediaPlayer.vlcVout.setWindowSize(width, height)
        val videoAspectRatio =
            surfaceView.measuredWidth.toFloat() / surfaceView.measuredHeight
        val newWidth: Int
        val newHeight: Int

        if (width > height) {
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                newWidth = width * 2
                newHeight = (width / videoAspectRatio * 2).toInt()
            } else {
                newWidth = width
                newHeight = (width / videoAspectRatio).toInt()
            }
        } else {
            newWidth = (height * videoAspectRatio).toInt()
            newHeight = height
        }

        val layoutParams: ViewGroup.LayoutParams = surfaceView.layoutParams
        layoutParams.width = newWidth
        layoutParams.height = newHeight
        surfaceView.layoutParams = layoutParams
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        mediaPlayer.release()
        libVLC.release()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val isPlaying = savedInstanceState.getBoolean("isPlaying", false)

        if (isPlaying && mediaPlayer.isPlaying) {
            mediaPlayer.play()
        }
    }

    override fun onPause() {
        super.onPause()
        val fragmentManager: FragmentManager = supportFragmentManager
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
        } else {
            super.finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }

        if (::libVLC.isInitialized) {
            libVLC.release()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::mediaPlayer.isInitialized && mediaPlayer.media != null) {
            try {
                outState.putBoolean("isPlaying", mediaPlayer.isPlaying)
            } catch (e: IllegalStateException) {
                outState.putBoolean("isPlaying", false)
            }
        } else {
            outState.putBoolean("isPlaying", false)
        }
    }

    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, i: Int, i1: Int) {}
    override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, i: Int, i1: Int) {}
    override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
        return false
    }

    override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}
}