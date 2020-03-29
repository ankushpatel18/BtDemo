package com.example.bluetoothcalc.model

import android.os.Parcelable
import com.example.bluetoothcalc.Actions
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CalcRequestModel(
    @SerializedName("action")
    var action: Actions,
    @SerializedName("val_one")
    var valueOne: Float,
    @SerializedName("val_two")
    var valueTwo: Float,
    @SerializedName("result")
    var result: Float = -1f
) : Parcelable