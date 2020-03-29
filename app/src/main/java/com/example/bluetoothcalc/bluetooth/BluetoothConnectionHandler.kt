package com.example.bluetoothcalc.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.example.bluetoothcalc.bluetooth.requests.*
import java.lang.IllegalArgumentException


/**
 * Handles all bluetooth requests
 * */
class BluetoothConnectionHandler(context: Context) :
    IBluetoothEventListener {

    //Singleton object
    companion object {
        private var mInstance: BluetoothConnectionHandler? = null
        const val TAG = "BtConnectionHandler"
        @Synchronized
        fun getInstance(context: Context): BluetoothConnectionHandler {
            if (mInstance == null) {
                mInstance = BluetoothConnectionHandler(context)
            }
            mInstance!!.enableBluetoothAdapter()    //Enable bluetooth if not enabled
            mInstance!!.startServer()
            return mInstance!!
        }
    }

    init {
        Log.d(TAG, "new instance created")
    }
    private var eventListener: IBluetoothEventListener? = null

    //All bluetooth request classes
    private val enableRequest =
        EnableRequest(context, this)
    private val discoverRequest =
        DiscoverRequest(context, this)
    private val pairRequest =
        PairRequest(context, this)
    private val connectionRequest =
        ConnectionRequest(context, this)


    fun setEventListener(eventListener: IBluetoothEventListener) {
        this.eventListener = eventListener
    }

    fun enableBluetoothAdapter() {
        enableRequest.enableBluetooth()
    }

    fun enableBluetoothDiscovery(activity: Activity) {
        enableRequest.enableDiscovery(activity)
    }

    fun discoverDevices() {
        discoverRequest.discover()
    }

    fun pairDevice(device: BluetoothDevice) {
        pairRequest.pair(device)
    }

    fun connectDevice(device: BluetoothDevice?) {
    }

    fun sendMsg(msg: String) {
        connectionRequest.sendMsg(encryptString(msg))
    }

    /**
     * Closes the connection with remote device
     * */
    fun connectionCleanup() {
        connectionRequest.cleanup()
    }


    fun cleanUp() {
        try {
            enableRequest.cleanup()
            discoverRequest.cleanup()
            pairRequest.cleanup()

        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Error in unregistering receiver $e")
        }

    }

    fun startServer() {
        connectionRequest.startServer()
    }

    override fun onEnable() {
        eventListener?.onEnable()
    }

    override fun onDeviceDiscovered(device: BluetoothDevice, rssi: Short) {
        eventListener?.onDeviceDiscovered(device, rssi)
    }

    override fun onDiscoveryFinished() {
        eventListener?.onDiscoveryFinished()
    }

    override fun onConnecting() {
        eventListener?.onConnecting()
    }

    override fun onConnectionResult(isSuccess: Boolean, device: BluetoothDevice) {
        eventListener?.onConnectionResult(isSuccess, device)
    }

    override fun onConnectedAsServer() {
        eventListener?.onConnectedAsServer()
    }

    override fun onPaired(device: BluetoothDevice) {
        eventListener?.onPaired(device)
    }

    override fun onDisconnecting() {
        eventListener?.onDisconnecting()
    }

    override fun onDisconnected() {
        eventListener?.onDisconnected()
    }

    override fun onNewMsg(newMsg: String) {
        eventListener?.onNewMsg(decryptString(newMsg))
    }


}