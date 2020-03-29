package com.example.bluetoothcalc.view

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothcalc.R
import com.example.bluetoothcalc.model.BluetoothResult

class DevicesAdapter(
    private val requestList: ArrayList<BluetoothResult>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<DevicesAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onDeviceClicked(item: BluetoothResult)
    }


    override fun onBindViewHolder(userViewHolder: ViewHolder, position: Int) {
        val requests = requestList[position]
        userViewHolder.tvDeviceName.text = requests.name
        userViewHolder.tvMacAdd.text = requests.macAdd
        userViewHolder.tvRssi.text = requests.rssi.toString()
        userViewHolder.itemView.setOnClickListener {
            listener.onDeviceClicked(requests)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_devices, parent, false)
        return ViewHolder(view)
    }


    override fun getItemCount(): Int {
        return requestList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDeviceName: TextView = itemView.findViewById(R.id.tvDeviceName)
        val tvMacAdd: TextView = itemView.findViewById(R.id.tvMacAddress)
        val tvRssi: TextView = itemView.findViewById(R.id.tvRssi)
    }
}


