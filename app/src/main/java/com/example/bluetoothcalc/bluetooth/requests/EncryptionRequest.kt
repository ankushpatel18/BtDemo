package com.example.bluetoothcalc.bluetooth.requests

import android.util.Base64
import android.util.Log

/**
 * Returns encrypted string of given string
 * */
const val TAG = "EncryptionRequest"

fun encryptString(input: String): String {
    val data = input.toByteArray(Charsets.UTF_8)
    val output = Base64.encodeToString(data, Base64.DEFAULT)
    Log.i(TAG, "message after encryption $output")
    return output
}

/**
 * Returns decrypted string of given encrypted string
 * */
fun decryptString(input: String): String {
    val data = Base64.decode(input, Base64.DEFAULT)
    val output = data.toString(Charsets.UTF_8)
    Log.i(TAG, "message after decryption $output")
    return output
}
