package com.android.rurutekcamview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.database.sqlite.SQLiteDatabase
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CameraSingleAdapter(
    private val context: Context,
    private val cameraInfoList: List<CameraInfo>,
    private val dataChangeListener: CameraDataChangeListener
) : BaseExpandableListAdapter() {

    companion object {
        private const val EDIT_REQUEST_CODE = 1
    }

    override fun getGroupCount(): Int = cameraInfoList.size

    override fun getChildrenCount(groupPosition: Int): Int = 1

    override fun getGroup(groupPosition: Int): Any = cameraInfoList[groupPosition]

    override fun getChild(groupPosition: Int, childPosition: Int): Any = cameraInfoList[groupPosition]

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()

    override fun hasStableIds(): Boolean = true

    @SuppressLint("InflateParams")
    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_group, null)

        val cameraName: TextView = view.findViewById(R.id.textCameraName)
        val ipAddress: TextView = view.findViewById(R.id.textIpAddress)
        val streamingPort: TextView = view.findViewById(R.id.textStreamingPort)

        val cameraInfo = getGroup(groupPosition) as CameraInfo

        cameraName.text = cameraInfo.cameraName
        ipAddress.text = cameraInfo.ipAddress
        streamingPort.text = cameraInfo.streamingPath

        return view
    }

    @SuppressLint("InflateParams")
    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_child_2, null)

        val playBtn: ImageButton = view.findViewById(R.id.play)
        val editBtn: ImageButton = view.findViewById(R.id.edit)
        val rotateBtn: ImageButton = view.findViewById(R.id.rotate)
        val deleteBtn: ImageButton = view.findViewById(R.id.delete)

        val cameraInfo = getChild(groupPosition, childPosition) as CameraInfo

        playBtn.setOnClickListener {
            if (isConnectedToNetwork()) {
                handleViewCamera(cameraInfo)
            } else {
                Toast.makeText(context.applicationContext, "Please connect to a Wifi Network", Toast.LENGTH_SHORT).show()
            }
        }

        editBtn.setOnClickListener { handleEditCamera(cameraInfo) }

        rotateBtn.setOnClickListener {
            if (isConnectedToNetwork()) {
                handleRotateCamera(cameraInfo)
            } else {
                Toast.makeText(context.applicationContext, "Please connect to a Wifi Network", Toast.LENGTH_SHORT).show()
            }
        }

        deleteBtn.setOnClickListener { handleDeleteCamera(cameraInfo) }

        return view
    }

    private fun isConnectedToNetwork(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = false

    private fun handleViewCamera(cameraInfo: CameraInfo) {
        val cameraViewsFragment = CameraViewsFragment()
        val args = Bundle()
        args.putParcelable("cameraInfo", cameraInfo)
        cameraViewsFragment.arguments = args

        val fragmentManager: FragmentManager = (context as AppCompatActivity).supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, cameraViewsFragment)
        fragmentTransaction.commit()
    }

    private fun handleEditCamera(cameraInfo: CameraInfo) {
        val intent = Intent(context, EditActivity::class.java)
        intent.putExtra("cameraInfo", cameraInfo)

        if (context is AppCompatActivity) {
            context.startActivityForResult(intent, EDIT_REQUEST_CODE, null)
        } else {
            context.startActivity(intent)
        }
    }

    private fun handleRotateCamera(cameraInfo: CameraInfo) {
        val intent = Intent(context, CameraViewsActivity::class.java)
        intent.putExtra("cameraInfo", cameraInfo)
        context.startActivity(intent)
    }

    private fun handleDeleteCamera(cameraInfo: CameraInfo) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Confirm Delete")
            .setMessage("Are you sure you want to delete ${cameraInfo.cameraName}?")
            .setPositiveButton("No") { dialog, _ -> dialog.dismiss() }
            .setNegativeButton("Yes") { _, _ -> cameraInfo.cameraName?.let { deleteCameraData(it) } }
            .show()
    }

    private fun deleteCameraData(cameraName: String) {
        Thread {
            val dbHelper = CameraDetailsDbHelper(context)
            val db: SQLiteDatabase = dbHelper.writableDatabase

            dbHelper.use {
                val whereClause = "${CameraDetailsDbHelper.CAMERA_NAME} = ?"
                val whereArgs = arrayOf(cameraName)

                val rowsDeleted = db.delete(CameraDetailsDbHelper.TABLE_NAME, whereClause, whereArgs)

                if (rowsDeleted > 0) {
                    dataChangeListener.onCameraDataChanged()
                    (context as LiveGridShowActivity).updateLiveFragment()
                } else {
                    Toast.makeText(context, "Failed to delete Camera", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}
