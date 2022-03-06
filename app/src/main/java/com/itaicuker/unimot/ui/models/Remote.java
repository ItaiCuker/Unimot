package com.itaicuker.unimot.ui.models;

import android.bluetooth.BluetoothDevice;

public class Remote
{
    private String name;
    private BluetoothDevice bluetoothDevice;

    public Remote(String name, BluetoothDevice bluetoothDevice) {
        this.name = name;
        this.bluetoothDevice = bluetoothDevice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }
}
