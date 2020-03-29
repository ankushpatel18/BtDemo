package com.example.bluetoothcalc.view

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.bluetoothcalc.Actions
import com.example.bluetoothcalc.Constants
import com.example.bluetoothcalc.R
import com.example.bluetoothcalc.bluetooth.BluetoothConnectionHandler
import com.example.bluetoothcalc.bluetooth.IBluetoothEventListener
import com.example.bluetoothcalc.model.BluetoothResult
import com.example.bluetoothcalc.model.CalcRequestModel
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import kotlinx.android.synthetic.main.activity_main.*

/**
 * displays calculator options
 * */
class MainActivity : AppCompatActivity(), IBluetoothEventListener {

    //flag to check if remote app is connected or not
    private var hasBtConnection = MutableLiveData<Boolean>()

    private lateinit var bluetoothConnection: BluetoothConnectionHandler
    private var selectedDevice: BluetoothDevice? = null

    //static constants
    companion object {
        const val TAG = "MainActivity"
        const val REQ_DEVICE_ACTIVITY = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bluetoothConnection = BluetoothConnectionHandler.getInstance(this)
        bluetoothConnection.enableBluetoothDiscovery(this)  //Enable discovery for 5 minutes

        checkLocationPermission()
        listenToDeviceConnectionChanges()
        clickListeners()        //Listen to button clicks
    }

    private fun clickListeners() {
        btnConnectToDevice.setOnClickListener {
            connectToSecondBtDevice()
        }


    }

    /**
     * Listen when remote device is connected and disconnected
     */
    private fun listenToDeviceConnectionChanges() {
        hasBtConnection.observe(this, Observer {
            btnConnectToDevice.visibility = View.GONE
            if (selectedDevice != null) {
                tvDeviceInfo.text = resources.getString(R.string.bluetooth_device_is_connected)
            } else {
                tvDeviceInfo.text =
                    resources.getString(R.string.bluetooth_device_is_connected_as_server)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        bluetoothConnection.setEventListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothConnection.connectionCleanup()
        bluetoothConnection.cleanUp()
    }


    /*
    * Navigate to bluetooth device listing screen
    * */
    private fun connectToSecondBtDevice() {
        val intent = Intent(this, BluetoothDevicesActivity::class.java)
        startActivityForResult(intent, REQ_DEVICE_ACTIVITY)
    }


    /*
    * Bluetooth handler callbacks started
    * */

    override fun onEnable() {
        //On bluetooth enabled
    }

    override fun onDeviceDiscovered(device: BluetoothDevice, rssi: Short) {
        //on new device discovered
    }

    override fun onDiscoveryFinished() {
        //On device discovery finished
        Log.d(TAG, "Device discovery finished")
    }

    override fun onConnecting() {
        Log.d(TAG, "Device is trying to connect")
    }

    override fun onConnectionResult(isSuccess: Boolean, device: BluetoothDevice) {
        if (isSuccess) {
            //Device is connected
            selectedDevice = device
        }
    }

    override fun onConnectedAsServer() {
        runOnUiThread {
            hasBtConnection.value = true
        }
    }


    override fun onPaired(device: BluetoothDevice) {
        bluetoothConnection.connectDevice(device)
    }

    override fun onDisconnecting() {
        //On disconnect flow with a device started
    }

    override fun onDisconnected() {
        Log.d(TAG, "Device is disconnected")
        selectedDevice = null
        hasBtConnection.value = false
    }

    override fun onNewMsg(newMsg: String) {
        if (newMsg.isBlank()) {
            //NO valid string
            return
        }
        runOnUiThread {
            Toast.makeText(this, "New message $newMsg", Toast.LENGTH_SHORT).show()
        }
        try {
            var dataReceived = Gson().fromJson(newMsg, CalcRequestModel::class.java)
            if (dataReceived.result == -1f) {       //another device is requesting calculation
                dataReceived = calculateNumbers(dataReceived)
                displayRemoteCalculation(dataReceived)
                bluetoothConnection.sendMsg(Gson().toJson(dataReceived))
            } else {
                displayResult(dataReceived.result)          //calculated result received from remote device
            }

        } catch (e: JsonParseException) {
            e.printStackTrace()
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
        }
    }

    /*
    * End of Bluetooth handler callbacks
    * */


    /*
    * Display device calculation result received from remote device
    * */
    private fun displayResult(result: Float) {

    }


    /*
    * Display remote device calculation data on UI
    * */
    private fun displayRemoteCalculation(dataReceived: CalcRequestModel) {

    }


    /*
    * Performs appropriate calculation operations and returns output object
    * */
    fun calculateNumbers(dataReceived: CalcRequestModel): CalcRequestModel {
        when (dataReceived.action) {
            Actions.DIVIDE -> {
                val output = dataReceived.valueOne / dataReceived.valueTwo
                dataReceived.result = output
            }
            Actions.MULTIPLY -> {
                val output = dataReceived.valueOne * dataReceived.valueTwo
                dataReceived.result = output
            }
        }
        return dataReceived
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_DEVICE_ACTIVITY && resultCode == Activity.RESULT_OK && data != null) {
            selectedDevice = data.getParcelableExtra(Constants.ARGS_DEVICE)
            hasBtConnection.value = true
        }
    }

}
