package com.example.bluetoothcalc.bluetooth.requests

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.example.bluetoothcalc.bluetooth.IBluetoothEventListener
import com.example.bluetoothcalc.bluetooth.IBluetoothRequest


/**
 * Manages all requests related to bluetooth discovery for nearby devices
 * */
class DiscoverRequest(
    private val context: Context,
    private val eventListener: IBluetoothEventListener
) :
    IBluetoothRequest {

    companion object {
        private const val TAG = "DiscoverRequest"
    }

//    private var discoveredMacAddresses: MutableList<String> = mutableListOf()
    private var bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var discoverReceiver : DeviceDiscoveryReceiver? = null

    init {
        registerReceiver()
    }

    /**
     * Scan for nearby bluetooth devices
     * */
    fun discover() {
//        discoveredMacAddresses = mutableListOf()
        if (bluetoothAdapter.isDiscovering)
            bluetoothAdapter.cancelDiscovery()

        bluetoothAdapter.startDiscovery()
    }

    /**
     * Register receiver to listen on device discovery actions
     * */
    private fun registerReceiver() {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        discoverReceiver = DeviceDiscoveryReceiver()
        context.registerReceiver(
            discoverReceiver,
            filter,
            "android.permission.BLUETOOTH_ADMIN",
            null
        )

    }

    /**
     * Check and add new found device if not already exists
     * */
    private fun addDiscoveredDevice(newDevice: BluetoothDevice, rssi: Short) {
//        if (!discoveredMacAddresses.contains(newDevice.address)) {
//            discoveredMacAddresses.add(newDevice.address)
            eventListener.onDeviceDiscovered(newDevice, rssi)
//        }
    }

    /**
     * Unregister receivers
     * */
    override fun cleanup() {
        if (discoverReceiver != null) {
            context.unregisterReceiver(discoverReceiver)
        }
    }

    /**
     * BroadcastReceiver to handle device discovery actions
     * */
    private inner class DeviceDiscoveryReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            Log.d(TAG, "new intent found: $intent")
            if (action == null) return

            when (action) {
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.i(TAG, "ACTION_DISCOVERY_FINISHED")
                    eventListener.onDiscoveryFinished()
                }

                BluetoothDevice.ACTION_FOUND -> {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE)
                    addDiscoveredDevice(device, rssi)
                }
            }
        }
    }
}