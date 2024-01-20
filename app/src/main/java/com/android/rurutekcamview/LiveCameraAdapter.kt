package com.android.rurutekcamview


import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.graphics.SurfaceTexture
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LiveCameraAdapter(private val mContext: Context, private var mCursor: Cursor?) :
    RecyclerView.Adapter<LiveCameraAdapter.LiveCameraViewHolder>() {

    private val executorService: ExecutorService = Executors.newFixedThreadPool(5)

    inner class LiveCameraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textureView: TextureView = itemView.findViewById(R.id.textureView)
        val cameraNameTextView: TextView = itemView.findViewById(R.id.cameraNameTV)
        val ipAddressTextView: TextView = itemView.findViewById(R.id.ipAddressTV)
        val libVLC: LibVLC = LibVLC(mContext)
        val mediaPlayer: MediaPlayer = MediaPlayer(libVLC)

        init {
            itemView.setOnClickListener {
                val position: Int = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    if (mCursor?.moveToPosition(position) == true) {
                        val cameraName: String =
                            mCursor!!.getString(mCursor!!.getColumnIndexOrThrow(CameraDetailsDbHelper.CAMERA_NAME))
                        val ipAddress: String =
                            mCursor!!.getString(mCursor!!.getColumnIndexOrThrow(CameraDetailsDbHelper.IP_ADDRESS))
                        val username: String? =
                            mCursor!!.getString(mCursor!!.getColumnIndexOrThrow(CameraDetailsDbHelper.RTSP_USERNAME))
                        val password: String? =
                            mCursor!!.getString(mCursor!!.getColumnIndexOrThrow(CameraDetailsDbHelper.RTSP_PASSWORD))
                        val streamingPath: String? =
                            mCursor!!.getString(mCursor!!.getColumnIndexOrThrow(CameraDetailsDbHelper.STREAMING_PATH))

                        val toastMessage =
                            "Camera Name: $cameraName\nIP Address: $ipAddress"
                        Toast.makeText(mContext, toastMessage, Toast.LENGTH_SHORT).show()

                        val cameraDetailsFragment = CameraDetailsFragment()
                        val args = Bundle()
                        args.putString("cameraName", cameraName)
                        args.putString("ipAddress", ipAddress)
                        args.putString("username", username)
                        args.putString("password", password)
                        args.putString("streamingPath", streamingPath)

                        args.also { cameraDetailsFragment.arguments = it }

                        val fragmentManager: FragmentManager =
                            (mContext as AppCompatActivity).supportFragmentManager
                        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
                        fragmentTransaction.replace(R.id.frameLayout, cameraDetailsFragment)
                        fragmentTransaction.commit()
                    }
                }
            }
        }

        fun initializeMediaPlayer(rtspUrl: String) {
            textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(
                    surface: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {
                    mediaPlayer.vlcVout.setVideoSurface(Surface(surface), null)
                    mediaPlayer.vlcVout.attachViews()

                    executorService.execute {
                        try {
                            if (textureView.isAvailable) {
                                val media = Media(libVLC, Uri.parse(rtspUrl))
                                mediaPlayer.media = media
                                mediaPlayer.time = 10000
                                mediaPlayer.play()
                                mediaPlayer.volume = 0
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onSurfaceTextureSizeChanged(
                    surface: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {
                }

                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                    mediaPlayer.vlcVout.detachViews()
                    surface.release()
                    return true
                }

                override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
            }
        }

        fun releaseMediaPlayer() {
            mediaPlayer.stop()
            mediaPlayer.vlcVout.detachViews()
            libVLC.release()
        }

        fun updateSurfaceViewPosition() {
            mediaPlayer.vlcVout
            mediaPlayer.vlcVout.setWindowSize(textureView.width, textureView.height)

            val videoAspectRatio =
                textureView.width.toFloat() / textureView.height
            val newWidth = textureView.width
            val newHeight = (textureView.width / videoAspectRatio).toInt()

            val layoutParams: ViewGroup.LayoutParams = textureView.layoutParams
            layoutParams.width = newWidth
            layoutParams.height = newHeight
            textureView.layoutParams = layoutParams
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveCameraViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(mContext)
        val view: View = inflater.inflate(R.layout.item_camera, parent, false)
        return LiveCameraViewHolder(view)
    }

    override fun onBindViewHolder(holder: LiveCameraViewHolder, position: Int) {
        if (position < itemCount) {
            if (mCursor?.moveToPosition(position) == true) {
                val cameraName: String =
                    mCursor!!.getString(mCursor!!.getColumnIndexOrThrow(CameraDetailsDbHelper.CAMERA_NAME))
                val username: String? =
                    mCursor!!.getString(mCursor!!.getColumnIndexOrThrow(CameraDetailsDbHelper.RTSP_USERNAME))
                val password: String? =
                    mCursor!!.getString(mCursor!!.getColumnIndexOrThrow(CameraDetailsDbHelper.RTSP_PASSWORD))
                val ipAddress: String =
                    mCursor!!.getString(mCursor!!.getColumnIndexOrThrow(CameraDetailsDbHelper.IP_ADDRESS))
                val streamingPath: String? =
                    mCursor!!.getString(mCursor!!.getColumnIndexOrThrow(CameraDetailsDbHelper.STREAMING_PATH))
                val rtspUrl =
                    "rtsp://$username:$password@$ipAddress/$streamingPath"

                holder.releaseMediaPlayer()
                holder.initializeMediaPlayer(rtspUrl)
                holder.cameraNameTextView.text = cameraName
                holder.ipAddressTextView.text = ipAddress
            }
        } else {
            holder.releaseMediaPlayer()
            holder.textureView.visibility = View.INVISIBLE
            holder.cameraNameTextView.text = ""
            holder.ipAddressTextView.text = ""
        }
    }

    override fun getItemCount(): Int {
        return mCursor?.count ?: 0
    }

    @SuppressLint("NotifyDataSetChanged")
    fun swapCursor(newCursor: Cursor?) {
        mCursor?.close()
        mCursor = newCursor
        newCursor?.let {
            notifyDataSetChanged()
        }
    }
}