package com.example.bluetoothcalc.model

import android.os.Parcelable
import com.example.bluetoothcalc.Actions
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BluetoothResult(
    @SerializedName("rssi")
    var rssi: Short,
    @SerializedName("mac")
    var macAdd: String,
    @SerializedName("name")
    var name: String,
    @SerializedName("bondState")
    var bondState: Int
) : Parcelable