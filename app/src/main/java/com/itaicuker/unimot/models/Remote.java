package com.itaicuker.unimot.models;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

public class Remote
{
    private String id;
    private boolean isOnline;
    private BluetoothDevice bluetoothDevice;

    public Remote(String id, boolean isOnline) {
        this.id = id;
    }

    public Remote(String id, BluetoothDevice bluetoothDevice) {
        this.id = id;
        this.bluetoothDevice = bluetoothDevice;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public boolean isOnline() {
        return isOnline;
    }

    @NonNull
    @Override
    public String toString() {
        return id;
    }
}
