package com.itaicuker.unimot.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * represents a device group
 */
public class Group {
    private String name;
    private int id;
    private final List<Device> devices;

    /**
     * create a group
     * @param name of group
     * @param id of group
     * @param devices list of devices in group
     */
    public Group(String name, int id, @Nullable List<Device> devices) {
        this.name = name;
        this.id = id;
        this.devices = (devices == null) ? new ArrayList<>() : devices;
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

    public List<Device> getDevices() {
        return devices;
    }

    /**
     * add to group
     * @param device to add.
     */
    public void addDevice(@NonNull Device device){
        devices.add(device);
    }

    /**
     * remove from group
     * @param device to remove.
     */
    public void removeDevice(@NonNull Device device){
        devices.remove(device);
    }
}
