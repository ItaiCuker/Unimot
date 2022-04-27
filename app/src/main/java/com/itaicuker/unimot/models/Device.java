package com.itaicuker.unimot.models;


import androidx.annotation.Nullable;

import com.google.firebase.firestore.IgnoreExtraProperties;

/**
 * represents a device
 */
@IgnoreExtraProperties
public class Device {
    /**
     *
     */
    private String name;
    private DeviceType deviceType;

    public Device(){

    }

    public Device(@Nullable String name, @Nullable DeviceType deviceType) {
        this.name = name;
        this.deviceType = deviceType;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }
}
