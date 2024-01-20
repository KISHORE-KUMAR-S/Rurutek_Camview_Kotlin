package com.android.rurutekcamview

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment

class ItemFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_item, container, false)

        val cameraList = view.findViewById<Button>(R.id.savedDevices)
        val savedDevicesLayout = view.findViewById<Button>(R.id.savedDevicesLayout)

        cameraList.setOnClickListener {
            val intent = Intent(
                requireContext(),
                CameraListActivity::class.java
            )
            startActivity(intent)
        }

        savedDevicesLayout.setOnClickListener {
            if (isConnectedToNetwork()) {
                startLiveGridShowActivity()
            } else {
                showToast()
            }
        }

        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showExitConfirmationDialog()
                }
            })

        return view
    }

    private fun startLiveGridShowActivity() {
        Thread {
            val intent = Intent(
                requireContext(),
                LiveGridShowActivity::class.java
            )
            startActivity(intent)
        }.start()
    }

    private fun showExitConfirmationDialog() {
        val dialogFragment = ExitConfirmationDialogFragment()
        dialogFragment.show(childFragmentManager, "ExitConfirmationDialogFragment")
    }

    private fun isConnectedToNetwork(): Boolean {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private fun showToast() {
        requireActivity().runOnUiThread {
            Toast.makeText(
                requireContext(),
                "Please connect to a Wifi Network",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}