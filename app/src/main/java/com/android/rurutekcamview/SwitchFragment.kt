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


class SwitchFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_switch, container, false)
        val addDevice = view.findViewById<Button>(R.id.addManually)
        val discoverDevices = view.findViewById<Button>(R.id.discoverDevices)
        addDevice.setOnClickListener {
            if (isConnectedToNetwork()) {
                requireActivity().runOnUiThread {
                    val intent =
                        Intent(requireContext(), AddActivity::class.java)
                    startActivity(intent)
                }
            } else {
                showToast("Please connect to a Wifi Network")
            }
        }
        discoverDevices.setOnClickListener {
            if (isConnectedToNetwork()) {
                requireActivity().runOnUiThread {
                    val intent = Intent(
                        requireContext(),
                        DiscoverActivity::class.java
                    )
                    startActivity(intent)
                }
            } else {
                showToast("Please connect to a Wifi network")
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

    private fun showToast(message: String) {
        requireActivity().runOnUiThread {
            Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_SHORT
            ).show()
        }
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
}