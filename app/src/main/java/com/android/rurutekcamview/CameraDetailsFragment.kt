package com.android.rurutekcamview

import android.graphics.SurfaceTexture
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer

class CameraDetailsFragment : Fragment(), TextureView.SurfaceTextureListener {

    private lateinit var libVLC: LibVLC
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var textureView: TextureView
    private lateinit var rtspUrl: String
    private lateinit var viewModel: CameraDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[CameraDetailsViewModel::class.java]
        rtspUrl = viewModel.rtspUrl.toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_camera_views, container, false)

        textureView = view.findViewById(R.id.textureView)
        textureView.surfaceTextureListener = this

        libVLC = LibVLC(requireContext())
        mediaPlayer = MediaPlayer(libVLC)

        arguments?.let { args ->
            val cameraName = args.getString("cameraName")
            val ipAddress = args.getString("ipAddress")
            val username = args.getString("username")
            val password = args.getString("password")
            val streamingPath = args.getString("streamingPath")

            if (cameraName != null && ipAddress != null && username != null && password != null && streamingPath != null) {
                rtspUrl = "rtsp://$username:$password@$ipAddress/$streamingPath"
                viewModel.rtspUrl = rtspUrl
            }

            val media = Media(libVLC, Uri.parse(rtspUrl))
            mediaPlayer.media = media
            mediaPlayer.play()
            mediaPlayer.volume = 0
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releaseResources()
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        mediaPlayer.vlcVout.setVideoSurface(Surface(surface), null)
        mediaPlayer.vlcVout.attachViews()

        textureView.let {
            mediaPlayer.vlcVout.setWindowSize(it.width, it.height)

            val videoAspectRatio = it.width.toFloat() / it.height
            val newWidth = it.width
            val newHeight = (it.width / videoAspectRatio).toInt()

            val layoutParams = it.layoutParams
            layoutParams.width = newWidth
            layoutParams.height = newHeight
            it.layoutParams = layoutParams
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        mediaPlayer.release()
        libVLC.release()
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

    override fun onDestroy() {
        super.onDestroy()
        releaseResources()
    }

    private fun releaseResources() {
        mediaPlayer.release()
        libVLC.release()
    }
}
