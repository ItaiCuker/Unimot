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
    private boolean isFavorite;
    private DeviceType deviceType;

    public Device(String name, String id, boolean isFavorite, DeviceType deviceType) {
        this.name = name;
        this.id = id;
        this.isFavorite = isFavorite;
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

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }
}
