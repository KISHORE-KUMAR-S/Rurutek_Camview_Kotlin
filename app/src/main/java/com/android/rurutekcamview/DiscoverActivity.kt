package com.android.rurutekcamview

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.util.TreeSet
import java.util.UUID
import java.util.regex.Matcher
import java.util.regex.Pattern

class DiscoverActivity : AppCompatActivity() {
    private var listView: ListView? = null
    private var responses: ArrayList<String>? = null
    private var adapter: ArrayAdapter<String>? = null
    private var progressBar: ProgressBar? = null
    private var viewModel: DiscoverViewModel? = null
    private var noOfDevicesDetected: TextView? = null

    companion object {
        const val WS_DISCOVERY_TIMEOUT = 4000
        const val WS_DISCOVERY_PORT = 3702
        const val WS_DISCOVERY_ADDRESS_IPv4 = "239.255.255.250"
        const val KEY_PROGRESS_BAR_VISIBLE = "progressBarVisible"
        const val IPV4_PATTERN = "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b"
        const val WS_DISCOVERY_PROBE_MESSAGE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:tds=\"http://www.onvif.org/ver10/device/wsdl\" xmlns:tns=\"http://schemas.xmlsoap.org/ws/2005/04/discovery\" xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">\r\n" +
                "   <soap:Header>\r\n" +
                "      <wsa:Action>http://schemas.xmlsoap.org/ws/2005/04/discovery/Probe</wsa:Action>\r\n" +
                "      <wsa:MessageID>urn:uuid:5e1cec36-03b9-4d8b-9624-0c5283982a00</wsa:MessageID>\r\n" +
                "      <wsa:To>urn:schemas-xmlsoap-org:ws:2005:04:discovery</wsa:To>\r\n" +
                "   </soap:Header>\r\n" +
                "   <soap:Body>\r\n" +
                "      <tns:Probe>\r\n" +
                "         <tns:Types>tds:Device</tns:Types>\r\n" +
                "      </tns:Probe>\r\n" +
                "   </soap:Body>\r\n" +
                "</soap:Envelope>"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discover)

        setStatusBarTextColor()

        viewModel = ViewModelProvider(this)[DiscoverViewModel::class.java]

        val refresh = findViewById<TextView>(R.id.refresh)
        progressBar = findViewById(R.id.progressBar)
        listView = findViewById(R.id.listview)
        noOfDevicesDetected = findViewById(R.id.totalDevices)

        responses = ArrayList()

        adapter = ArrayAdapter(this, R.layout.discover_list_item, R.id.cameraIP, responses!!)
        listView?.adapter = adapter

        refresh?.setOnClickListener { refreshList() }

        if (viewModel!!.responses.isEmpty()) {
            View.VISIBLE.also { progressBar?.visibility = it }
            val uuid = UUID.randomUUID().toString()
            ProbeTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, uuid)
        } else {
            responses!!.addAll(viewModel!!.responses)
            updateListView()
        }

        val back = findViewById<ImageButton>(R.id.back)
        back.setOnClickListener { finish() }
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

    private fun refreshList() {
        responses!!.clear()
        updateListView()
        progressBar!!.visibility = View.VISIBLE
        val uuid = UUID.randomUUID().toString()
        ProbeTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, uuid)
    }

    private fun updateListView() {
        if (adapter != null) {
            val uniqueResponses: Set<String> = TreeSet(responses)
            adapter!!.clear()
            adapter!!.addAll(uniqueResponses)
            listView!!.onItemClickListener =
                OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                    val ipAddress =
                        java.util.ArrayList(uniqueResponses)[position]
                    navigateToActivity(ipAddress)
                }
            adapter!!.notifyDataSetChanged()
            if (noOfDevicesDetected != null) {
                noOfDevicesDetected!!.text = uniqueResponses.size.toString()
            }
        }
    }

    private fun navigateToActivity(ipAddress: String) {
        val intent = Intent(this, AddActivity::class.java)
        intent.putExtra("ipAddress", ipAddress)
        startActivity(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(
            KEY_PROGRESS_BAR_VISIBLE,
            progressBar!!.visibility == View.VISIBLE
        )
    }


    @SuppressLint("StaticFieldLeak")
    private inner class ProbeTask : AsyncTask<String, Void, ArrayList<String>>() {
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg uuid: String): ArrayList<String>? {
            return try {
                getResponsesToProbe(uuid[0])
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }

        override fun onPostExecute(result: ArrayList<String>?) {
            super.onPostExecute(result)

            this@DiscoverActivity.progressBar?.visibility = View.GONE

            if (!result.isNullOrEmpty()) {
                runOnUiThread {
                    val uniqueResponses = responses?.let { HashSet(it) }
                    uniqueResponses?.addAll(result)
                    responses = ArrayList(uniqueResponses!!)
                    updateListView()
                    (responses as ArrayList<String>).also { this@DiscoverActivity.viewModel!!.responses = it }
                }
            }
        }

        private fun getResponsesToProbe(uuid: String): ArrayList<String> {
            val probe = WS_DISCOVERY_PROBE_MESSAGE.replace("<wsa:MessageID>urn:uuid:.*</wsa:MessageID>", "<wsa:MessageID>urn:uuid:$uuid</wsa:MessageID>")

            val port = 3702
            val senderAndReceiver = DatagramSocket(port)
            senderAndReceiver.soTimeout = WS_DISCOVERY_TIMEOUT

            val probeMsg = DatagramPacket(probe.toByteArray(), probe.length, InetAddress.getByName(WS_DISCOVERY_ADDRESS_IPv4), WS_DISCOVERY_PORT)
            senderAndReceiver.send(probeMsg)

            val uniqueResponses = HashSet<String>()
            val receiverBuffer = ByteArray(8192)
            val receiverPacket = DatagramPacket(receiverBuffer, receiverBuffer.size)

            while (true) {
                try {
                    senderAndReceiver.receive(receiverPacket)
                    val response = String(receiverPacket.data, 0, receiverPacket.length)

                    val pattern: Pattern = Pattern.compile(IPV4_PATTERN)
                    val matcher: Matcher = pattern.matcher(response)

                    while (matcher.find()) {
                        uniqueResponses.add(matcher.group())
                    }
                } catch (e: SocketTimeoutException) {
                    break
                }
            }

            senderAndReceiver.close()
            return ArrayList(uniqueResponses)
        }
    }

}