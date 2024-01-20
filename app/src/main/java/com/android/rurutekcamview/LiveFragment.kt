package com.android.rurutekcamview

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.rurutekcamview.LiveCameraAdapter.LiveCameraViewHolder


class LiveFragment : Fragment() {
    private var dbHelper: CameraDetailsDbHelper? = null
    private var recyclerView: RecyclerView? = null
    private var noWifiTV: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkConnectivityAndLoadVideo()
    }

    private fun checkConnectivityAndLoadVideo() {
        val connectivityManager =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            dbHelper = context?.let { CameraDetailsDbHelper(it) }
            if (recyclerView != null) {
                recyclerView!!.visibility = View.VISIBLE
                noWifiTV!!.visibility = View.GONE
            }
        } else {
            if (recyclerView != null) {
                recyclerView!!.visibility = View.GONE
                noWifiTV!!.visibility = View.VISIBLE
            }
        }
    }

    private val connectivityReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            checkConnectivityAndLoadVideo()
        }
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        requireContext().registerReceiver(connectivityReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(connectivityReceiver)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_live, container, false)

        dbHelper = context?.let { CameraDetailsDbHelper(it) }

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                updateVideoPosition()
            }
        })

        noWifiTV = view.findViewById(R.id.no_wifi_tv)

        if (recyclerView != null && noWifiTV != null) {
            if (dbHelper != null) {
                recyclerView!!.visibility = View.VISIBLE
                noWifiTV!!.visibility = View.GONE

                val gridCount = 2
                val layoutManager =
                    GridLayoutManager(context, gridCount, GridLayoutManager.HORIZONTAL, false)

                if (recyclerView?.adapter == null) {
                    val adapter = LiveCameraAdapter(requireContext(), null)
                    recyclerView?.adapter = adapter
                }

                recyclerView?.layoutManager = layoutManager

                val adapter = recyclerView?.adapter as LiveCameraAdapter?
                adapter?.swapCursor(dbHelper!!.allCameraDetails)
            }
        } else {
            recyclerView?.visibility = View.GONE
            noWifiTV?.visibility = View.VISIBLE
        }

        return view
    }


    private fun updateVideoPosition() {
        val layoutManager = recyclerView!!.layoutManager
        if (layoutManager is GridLayoutManager) {
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
            for (i in firstVisibleItemPosition..lastVisibleItemPosition) {
                val viewHolder = recyclerView!!.findViewHolderForAdapterPosition(i)
                if (viewHolder is LiveCameraViewHolder) {
                    viewHolder.updateSurfaceViewPosition()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (dbHelper != null) {
            dbHelper!!.close()
        }
    }
}