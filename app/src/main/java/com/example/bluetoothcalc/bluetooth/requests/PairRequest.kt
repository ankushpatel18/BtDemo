package com.example.bluetoothcalc.bluetooth.requests

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.example.bluetoothcalc.bluetooth.IBluetoothEventListener
import com.example.bluetoothcalc.bluetooth.IBluetoothRequest

/**
 * Manages all requests related to pairing with bluetooth device
 * */
class PairRequest(private val context : Context, private val eventListener: IBluetoothEventListener) :
    IBluetoothRequest {

    companion object {
        private const val TAG = "PairRequest"
    }
    private var isPairingInProgress = false
    private lateinit var currentBluetoothDevice : BluetoothDevice
    private var pairReceiver: BroadcastReceiver? = null

    //Default constructor
    init {
        registerReceiver()
    }

    /**
     * Start pairing with bluetooth device
     * */
    fun pair(device : BluetoothDevice) {
        if (isPairingInProgress)
            return
        isPairingInProgress = true
        currentBluetoothDevice = device
        device.createBond()
    }


    private fun registerReceiver() {
        pairReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val state = intent?.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                val prevState = intent?.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR)

                if (prevState == BluetoothDevice.BOND_BONDING && state == BluetoothDevice.BOND_BONDED) {
                    isPairingInProgress = false
                    Log.d(TAG, "New device paired $currentBluetoothDevice")
                    eventListener.onPaired(currentBluetoothDevice)
                }

            }
        }
        context.registerReceiver(pairReceiver, IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
    }


    override fun cleanup() {
            if (pairReceiver != null) {
                context.unregisterReceiver(pairReceiver)
            }
    }
}