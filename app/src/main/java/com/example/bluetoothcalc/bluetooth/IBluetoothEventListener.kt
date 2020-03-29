package com.example.bluetoothcalc.bluetooth

import android.bluetooth.BluetoothDevice


interface IBluetoothEventListener {
    fun onEnable()
    fun onDeviceDiscovered(device: BluetoothDevice, rssi: Short)
    fun onDiscoveryFinished()
    fun onConnecting()
    fun onConnectionResult(isSuccess: Boolean, device: BluetoothDevice)
    fun onConnectedAsServer()
    fun onPaired(device: BluetoothDevice)
    fun onDisconnecting()
    fun onDisconnected()
    fun onNewMsg(newMsg: String)

}