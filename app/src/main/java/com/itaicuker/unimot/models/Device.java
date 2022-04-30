package com.itaicuker.unimot.models;


import androidx.annotation.Nullable;

import com.google.firebase.firestore.IgnoreExtraProperties;

/**
 * represents a device
 */
@IgnoreExtraProperties
public class Device {

    private String uId;
    private String name;
    private DeviceType deviceType;

    public Device(){
        //empty constructor for Firestore
    }

    public Device(String uId, @Nullable String name, @Nullable DeviceType deviceType) {
        this.uId = uId;
        this.name = name;
        this.deviceType = deviceType;
    }

    @Nullable
    public String getuId() {
        return uId;
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Nullable
    public DeviceType getDeviceType() {
        return deviceType;
    }

    @Override
    public String toString() {
        return "Device {" +
                "uId='" + uId + '\'' +
                ", name='" + name + '\'' +
                ", deviceType=" + deviceType +
                '}';
    }
}
