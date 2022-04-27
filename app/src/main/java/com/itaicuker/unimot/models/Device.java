package com.itaicuker.unimot.models;


/**
 * represents a device
 */
public class Device {
    /**
     *
     */
    private final String id;
    private String name;
    private DeviceType deviceType;

    public Device(String name, String id, DeviceType deviceType) {
        this.name = name;
        this.id = id;
        this.deviceType = deviceType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }
}
