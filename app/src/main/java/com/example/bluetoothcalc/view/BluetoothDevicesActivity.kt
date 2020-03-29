package com.example.bluetoothcalc.view

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.example.bluetoothcalc.Constants
import com.example.bluetoothcalc.Constants.Companion.ARGS_DEVICE
import com.example.bluetoothcalc.R
import com.example.bluetoothcalc.bluetooth.BluetoothConnectionHandler
import com.example.bluetoothcalc.bluetooth.IBluetoothEventListener
import com.example.bluetoothcalc.model.BluetoothResult
import kotlinx.android.synthetic.main.activity_bluetooth_devices.*
import kotlin.collections.ArrayList


/**
 * Discover and displays list of all bluetooth devices
 * */
class BluetoothDevicesActivity : AppCompatActivity(),
    DevicesAdapter.OnItemClickListener,
    IBluetoothEventListener {

    companion object {
        private const val TAG = "BluetoothDeviceActivity"
    }
    private lateinit var devicesAdapter: DevicesAdapter
    private var allDevices = ArrayList<BluetoothResult>()
    private lateinit var bluetoothConnection: BluetoothConnectionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_devices)
        devicesAdapter = DevicesAdapter(allDevices, this)
        list_devices.adapter = devicesAdapter

        bluetoothConnection = BluetoothConnectionHandler.getInstance(this)
        bluetoothConnection.enableBluetoothDiscovery(this)  //Enable discovery for 5 minutes
        bluetoothConnection.setEventListener(this)

        checkLocationPermission()

    }

    override fun onResume() {
        super.onResume()
        bluetoothConnection.discoverDevices()
    }

    override fun onPause() {
        super.onPause()
        bluetoothConnection.cleanUp()
    }

    //Location permission code
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PermissionChecker.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, getString(R.string.grant_location_permission), Toast.LENGTH_SHORT)
                .show()
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                Constants.REQ_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.REQ_LOCATION_PERMISSION) {
            checkLocationPermission()
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        bluetoothConnection.cleanUp()
    }

    override fun onDeviceClicked(item: BluetoothResult) {
        //Device is already paired
        if (item.bondState == BluetoothDevice.BOND_BONDED) {
            updateProgress(resources.getString(R.string.connecting_device))
//            bluetoothConnection.connectDevice(item)
        } else {
            updateProgress(resources.getString(R.string.pairing_device))
//            bluetoothConnection.pairDevice(item)
        }
        progress_circular.visibility = View.GONE
    }


    override fun onEnable() {
        bluetoothConnection.discoverDevices()
    }

    private fun updateProgress(msg:String?) {
       Toast.makeText(this, msg!!, Toast.LENGTH_SHORT).show()
    }

    override fun onDeviceDiscovered(device: BluetoothDevice, rssi: Short) {
        for (listDevice in allDevices) {
            if (listDevice.macAdd == device.address)
                return
        }
        val btDevice = BluetoothResult(rssi, device.address, device.name, device.bondState)
        allDevices.add(btDevice)
        devicesAdapter.notifyItemInserted((allDevices.size - 1))
    }

    override fun onDiscoveryFinished() {
        allDevices.clear()
        devicesAdapter.notifyDataSetChanged()
        bluetoothConnection.discoverDevices()
    }

    override fun onConnecting() {
        updateProgress(resources.getString(R.string.connecting_device))
        Log.d(TAG, "Device is trying to connect")
    }

    override fun onConnectionResult(isSuccess: Boolean, device: BluetoothDevice) {
        if (isSuccess) {
            //Device is connected
            val resultIntent = intent
            resultIntent.putExtra(ARGS_DEVICE, device)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        } else {
            runOnUiThread{
                Toast.makeText(this, "Failed to connect! Please try to connect again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onConnectedAsServer() {
        Log.d(TAG, "Connected as server.")
    }
    override fun onPaired(device: BluetoothDevice) {
        bluetoothConnection.connectDevice(device)
    }

    override fun onDisconnecting() {
        Log.d(TAG, "On device disconnecting.")
    }

    override fun onDisconnected() {
        Log.d(TAG, "Device is disconnected")
    }
    override fun onNewMsg(newMsg: String) {
        Toast.makeText(this, "New message $newMsg", Toast.LENGTH_SHORT).show()
    }

}