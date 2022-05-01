package com.itaicuker.unimot.models;


import java.io.Serializable;

/**
 * represents a device
 */
public class Device implements Serializable {

    private String uId;
    private String name;
    private DeviceType deviceType;
    private String remoteId;
    private boolean isOnline;

    public Device(){
        //empty constructor for Firestore
    }

    public Device(String uId, String name, DeviceType deviceType, String remoteId, boolean isOnline) {
        this.uId = uId;
        this.name = name;
        this.deviceType = deviceType;
        this.remoteId = remoteId;
        this.isOnline = isOnline;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public String getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
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
