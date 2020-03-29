package com.example.bluetoothcalc.bluetooth.requests

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.bluetoothcalc.Constants.Companion.BASE_UUID
import com.example.bluetoothcalc.bluetooth.IBluetoothEventListener
import com.example.bluetoothcalc.bluetooth.IBluetoothRequest
import java.util.*
import android.content.IntentFilter
import java.io.*

/**
 * Manages all requests related to bluetooth connection with a device
 * */
class ConnectionRequest(
    private val context: Context,
    private val eventListener: IBluetoothEventListener
) :
    IBluetoothRequest {
    private var bluetoothClientThread: BluetoothClientThread? = null
    private var bluetoothServerThread: BluetoothServerThread? = null
    private var socket: BluetoothSocket? = null
    private var input: InputStream? = null
    private var out: OutputStream? = null
    private var currentDevice: BluetoothDevice? = null

    companion object {
        private const val TAG = "ConnectionRequest"
    }

    /**
     * BroadcastReceiver for bluetooth device connection updates
     * */
    private val connectionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    Log.d(TAG, "New device connected")
                    currentDevice  = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    Log.d(TAG, "Disconnected..")
                    eventListener.onDisconnected()
                }
            }
        }
    }

    //Default constructor
    init {
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        context.registerReceiver(connectionReceiver, filter)
    }


    /*
    * Starts server thread to continuously check for any device connection request
    * */
    fun startServer() {
        bluetoothServerThread = BluetoothServerThread()
        bluetoothServerThread!!.start()
    }

    /**
     * Establish a BT connection with selected device
     * */
    fun connect(device: BluetoothDevice) {
        eventListener.onConnecting()
        bluetoothClientThread =
            BluetoothClientThread(device)
            { isSuccess -> eventListener.onConnectionResult(isSuccess, device) }
        bluetoothClientThread?.start()
    }

    /*
    * Stop client thread
    * */
    private fun disconnectThread() {
        if (bluetoothClientThread != null)
            bluetoothClientThread?.cancel()
    }

    /**
     * Send a new message if bluetooth device is connected
     * */
    fun sendMsg(msg: String) {
        if (socket != null && socket!!.isConnected) {
            try {
                out!!.write(msg.toByteArray())
            } catch (e: IOException) {
                eventListener.onDisconnected()
            }
        }

    }

    /**
     * close all streams and sockets on finishing activity or application
     * */
    override fun cleanup() {
        if (socket != null && socket!!.isConnected) {
            socket!!.close()
        }
        if (input != null) {
            input!!.close()
        }
        if (out != null) {
            out!!.close()
        }
        context.unregisterReceiver(connectionReceiver)
        disconnectThread()
    }


    private inner class BluetoothClientThread(
        private val device: BluetoothDevice,
        private val onComplete: (isSuccess: Boolean) -> Unit
    ) : Thread() {

        private var bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        init {
            createSocket()
        }

        /**
         * Creates socket connection using Rfcomm to listen for connection requests
         * */
        private fun createSocket() {
            try {
                val uuid = UUID.fromString(BASE_UUID)
                socket = device.createRfcommSocketToServiceRecord(uuid)
            } catch (e: IOException) {
                Log.e(TAG, "device.createRfcommSocketToServiceRecord IOException $e")
            }

        }

        override fun run() {
            super.run()
            bluetoothAdapter.cancelDiscovery()
            try {
                if (socket != null) {
                    socket!!.connect()
                    out = socket!!.outputStream
                    input = (socket!!.inputStream)
                    onComplete(true)
                    this@ConnectionRequest.currentDevice = device
                    startInputStreamReader()
                }
            } catch (e: IOException) {
                Log.e(TAG, "Client socket connection exception $e")
                onComplete(false)
            }
        }

        fun cancel() {
            socket?.close()
        }
    }

    /**
     * server thread to listen for remote device requests
     * */
    inner class BluetoothServerThread : Thread() {
        private var serverSocket: BluetoothServerSocket? = null
        override fun run() {
            try {
                val btAdapter = BluetoothAdapter.getDefaultAdapter()
                if (btAdapter != null) {
                    this.serverSocket = btAdapter.listenUsingRfcommWithServiceRecord(
                        "test",
                        UUID.fromString(BASE_UUID)
                    ) // 1
                    socket = serverSocket!!.accept()
                    input = socket!!.inputStream
                    out = socket!!.outputStream
                    eventListener.onConnectedAsServer()
                }
                startInputStreamReader()
            } catch (e: Exception) {
                Log.e(TAG, "Cannot read data", e)
            } finally {
                socket?.close()
            }
        }
    }


    /**
     * continuously check for new messages from remote device
     * Long running task Must be called from background thread
     * */
    private fun startInputStreamReader() {
        do {
            try {
                val availableBytes = input!!.available()
                if (availableBytes > 0) {
                    val bytes = ByteArray(availableBytes)
                    Log.i("server", "Reading")
                    input!!.read(bytes, 0, availableBytes)
                    val text = String(bytes)
                    this@ConnectionRequest.eventListener.onNewMsg(text)
                    Log.i(TAG, "Message received: $text")
                }
            } catch (e: IOException) {
                Log.e(TAG, "Cannot read data", e)

            }
        } while (socket!!.isConnected)
    }

}