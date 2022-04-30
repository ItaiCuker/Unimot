package com.itaicuker.unimot.models;


/**
 * represents a device
 */
public class Device {

    private String uId;
    private String name;
    private DeviceType deviceType;

    public Device(){
        //empty constructor for Firestore
    }

    public Device(String uId, String name, DeviceType deviceType) {
        this.uId = uId;
        this.name = name;
        this.deviceType = deviceType;
    }

    public String getuId() {
        return uId;
    }

    public String getName() {
        return name;
    }

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
