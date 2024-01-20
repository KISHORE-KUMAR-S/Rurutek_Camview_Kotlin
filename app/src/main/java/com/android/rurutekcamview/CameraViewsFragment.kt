package com.android.rurutekcamview

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import java.lang.ref.WeakReference

class CameraViewsFragment : Fragment, TextureView.SurfaceTextureListener, Parcelable {
    private var libVLC: LibVLC? = null
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var textureView: TextureView
    private var rtspUrl: String? = null

    private val MEDIA_INITIALIZATION_SUCCESS = 1
    private val MEDIA_INITIALIZATION_FAILURE = 2
    private val fragmentWeakReference = WeakReference(this)

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            val fragment = fragmentWeakReference.get() ?: return

            when (msg.what) {
                MEDIA_INITIALIZATION_FAILURE -> Toast.makeText(
                    fragment.requireContext(),
                    "Media initialization failed",
                    Toast.LENGTH_SHORT
                ).show()
                MEDIA_INITIALIZATION_SUCCESS -> { /* Handle success if needed */ }
            }
        }
    }

    constructor(parcel: Parcel) : this() {
        rtspUrl = parcel.readString()
    }

    constructor() : super()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_camera_views, container, false)

        textureView = view.findViewById(R.id.textureView)
        textureView.surfaceTextureListener = this

        val cameraViewModel = ViewModelProvider(requireActivity()).get(CameraViewModel::class.java)
        arguments?.let {
            val cameraInfo = it.getParcelable<CameraInfo>("cameraInfo")

            if (cameraInfo != null) {
                cameraViewModel.setCameraInfo(cameraInfo)
                rtspUrl =
                    "rtsp://${cameraInfo.rtspUsername}:${cameraInfo.rtspPassword}@${cameraInfo.ipAddress}/${cameraInfo.streamingPath}"
            }
        }

        MediaInitializationTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

        return view
    }

    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
        val surface = Surface(surfaceTexture)
        mediaPlayer?.vlcVout?.setVideoSurface(surface, null)
        mediaPlayer?.vlcVout?.attachViews()

        if (textureView != null) {
            mediaPlayer?.vlcVout?.setWindowSize(textureView.width, textureView.height)

            val videoAspectRatio =
                textureView.width.toFloat() / textureView.height.toFloat()
            val newWidth = textureView.width
            val newHeight = (textureView.width / videoAspectRatio).toInt()

            val layoutParams = textureView.layoutParams
            layoutParams.width = newWidth
            layoutParams.height = newHeight
            textureView.layoutParams = layoutParams
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        // Handle size change if needed
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        releaseMediaPlayer()
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        // Handle texture update if needed
    }

    @SuppressLint("StaticFieldLeak")
    private inner class MediaInitializationTask : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg voids: Void): Boolean {
            try {
                libVLC = LibVLC(requireContext())
                mediaPlayer = MediaPlayer(libVLC)
                val media = Media(libVLC, Uri.parse(rtspUrl))

                mediaPlayer?.setMedia(media)
                mediaPlayer?.play()
                mediaPlayer?.volume = 0

                handler.sendEmptyMessage(MEDIA_INITIALIZATION_SUCCESS)
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                handler.sendEmptyMessage(MEDIA_INITIALIZATION_FAILURE)
                return false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releaseMediaPlayer()
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.release()
        libVLC?.release()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(rtspUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CameraViewsFragment> {
        override fun createFromParcel(parcel: Parcel): CameraViewsFragment {
            return CameraViewsFragment(parcel)
        }

        override fun newArray(size: Int): Array<CameraViewsFragment?> {
            return arrayOfNulls(size)
        }
    }
}
