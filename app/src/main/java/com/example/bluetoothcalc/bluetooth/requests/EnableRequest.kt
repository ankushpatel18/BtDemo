package com.example.bluetoothcalc.bluetooth.requests

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.example.bluetoothcalc.bluetooth.IBluetoothEventListener
import com.example.bluetoothcalc.bluetooth.IBluetoothRequest

/**
 * Manages all requests related to enabling and disabling bluetooth
 * */
class EnableRequest(private val context : Context, private val eventListener: IBluetoothEventListener) :
    IBluetoothRequest {

    private var requestingEnableBluetooth = false
    private lateinit var bluetoothAdapter : BluetoothAdapter
    private var enableReceiver: BroadcastReceiver? = null

    /**
     * Register receiver to listen bluetooth enabled or disabled actions
     * */
    private fun registerReceiver() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        enableReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action
                if (!requestingEnableBluetooth && BluetoothAdapter.ACTION_STATE_CHANGED != (action))
                    return
                requestingEnableBluetooth = false
                eventListener.onEnable()
            }
        }
        context.registerReceiver(enableReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
    }

    /**
     * Turn on device's bluetooth if not already enabled
     * */
    fun enableBluetooth() {
        registerReceiver()
        if (!bluetoothAdapter.isEnabled) {
            requestingEnableBluetooth = true
            bluetoothAdapter.enable()
        }
        else
            eventListener.onEnable()
    }

    /**
     * Enable discovery mode so nearby devices can find this device
     * */
    fun enableDiscovery(activity: Activity) {
        val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0)   //enabled for an hour
        }
        activity.startActivity(discoverableIntent)
    }


    override fun cleanup() {
        if (enableReceiver != null) {
            context.unregisterReceiver(enableReceiver)
        }
    }

}