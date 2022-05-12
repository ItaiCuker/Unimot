package com.itaicuker.unimot.models;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Map;

/**
 * represents a device
 */
public class Device implements Serializable {

    /**
     * The Remote id.
     */
    final String remoteId;
    /**
     * The Name.
     */
    String name;
    /**
     * The Device type.
     */
    DeviceType deviceType;
    /**
     * The Id.
     */
    String id;
    /**
     * The Is available.
     */
    boolean isAvailable;
    /**
     * The Commands.
     */
    Map<String, String> commands;

    /**
     * Instantiates a new Device.
     *
     * @param id          the id
     * @param name        the name
     * @param deviceType  the device type
     * @param remoteId    the remote id
     * @param isAvailable the is available
     * @param commands    the commands
     */
    public Device(String id, String name, DeviceType deviceType, String remoteId, boolean isAvailable, Map<String, String> commands) {
        this.id = id;
        this.name = name;
        this.deviceType = deviceType;
        this.remoteId = remoteId;
        this.isAvailable = isAvailable;
        this.commands = commands;
    }

    /**
     * constructor for device list.
     *
     * @param id         the id
     * @param name       the name
     * @param deviceType the device type
     * @param remoteId   the remote id
     */
    public Device(String id, String name, DeviceType deviceType, String remoteId) {
        this.id = id;
        this.name = name;
        this.deviceType = deviceType;
        this.remoteId = remoteId;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets device type.
     *
     * @return the device type
     */
    public DeviceType getDeviceType() {
        return deviceType;
    }

    /**
     * Sets device type.
     *
     * @param deviceType the device type
     */
    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    /**
     * Gets remote id.
     *
     * @return the remote id
     */
    public String getRemoteId() {
        return remoteId;
    }

    /**
     * Is available boolean.
     *
     * @return the boolean
     */
    public boolean isAvailable() {
        return isAvailable;
    }

    /**
     * Gets commands.
     *
     * @return the commands
     */
    public Map<String, String> getCommands() {
        return commands;
    }

    @NonNull
    @Override
    public String toString() {
        return "Device{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", deviceType=" + deviceType +
                ", remoteId='" + remoteId + '\'' +
                ", isAvailable=" + isAvailable +
                ", commands=" + commands +
                '}';
    }
}
