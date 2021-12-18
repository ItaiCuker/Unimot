package com.itaicuker.unimot.models;


/**
 * represents a device
 */
public class Device {
    private String name;
    private int id;
    private DeviceType type;
    private boolean isFavorite;

    public Device(String name, int id, DeviceType type, boolean isFavorite) {
        this.name = name;
        this.id = id;
        this.type = type;
        this.isFavorite = isFavorite;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public DeviceType getType() {
        return type;
    }

    public void setType(DeviceType type) {
        this.type = type;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

}
