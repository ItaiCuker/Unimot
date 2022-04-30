package com.itaicuker.unimot.models;

import android.bluetooth.BluetoothDevice;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Remote
{
    private String id;
    private BluetoothDevice bluetoothDevice;

    public Remote() {

    }

    public Remote(String id) {
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
}
