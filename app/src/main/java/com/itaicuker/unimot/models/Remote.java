package com.itaicuker.unimot.models;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.itaicuker.unimot.Repository;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The type Remote.
 */
public class Remote {
    /**
     * The Id.
     */
    final String id;
    /**
     * The Is available boolean.
     */
    boolean isAvailable;
    /**
     * The Bluetooth device.
     */
    BluetoothDevice bluetoothDevice;

    /**
     * The Repository.
     */
    Repository repository;

    /**
     * Instantiates a new Remote.
     *
     * @param id          the id
     * @param isAvailable the is available
     */
    public Remote(String id, boolean isAvailable) {
        this.id = id;
        this.isAvailable = isAvailable;
    }

    /**
     * Instantiates a new Remote.
     *
     * @param id              the id
     * @param bluetoothDevice the bluetooth device
     */
    public Remote(String id, BluetoothDevice bluetoothDevice) {
        this.id = id;
        this.bluetoothDevice = bluetoothDevice;
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
     * Gets bluetooth device.
     *
     * @return the bluetooth device
     */
    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
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
     * start learn command process
     *
     * @param remoteLearnListener event catcher for learn command
     * @param deviceId            to bind command to
     * @param commandName         to bind command to
     * @param commandId           the command id
     */
    public void startLearnCommand(RemoteLearnListener remoteLearnListener, String deviceId, String commandName, String commandId) {
        //starting learn
        repository = Repository.getInstance();
        repository.startListeningRemote(id, remoteLearnListener);

        JSONObject obj = new JSONObject();
        JSONObject command = new JSONObject();
        try {
            command.put("message", "start");
            command.put("deviceId", deviceId);
            command.put("commandName", commandName);
            command.put("commandId", commandId);

            obj.put("command", command);
            obj.put("remoteId", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //sending start learn command to remote
        repository.sendCommand(obj);
    }

    @NonNull
    @Override
    public String toString() {
        return getId();
    }

    /**
     * method to stop learn
     */
    public void stopLearn() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("message", "stop");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendCommand(obj);
    }

    /**
     * method to stop listen to remote
     */
    public void stopListen() {
        repository.stopListeningRemote();
    }

    /**
     * send command to remote
     *
     * @param command command data
     */
    public void sendCommand(JSONObject command) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("remoteId", id);
            obj.put("command", command);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        repository.sendCommand(obj);
    }

    /**
     * enum for event handling
     */
    public enum Event {
        /**
         * Learn start event.
         */
        LEARN_START,
        /**
         * Learn remote received command event.
         */
        LEARN_REMOTE_RECEIVED_COMMAND,
        /**
         * Learn remote tested command event.
         */
        LEARN_REMOTE_TESTED_COMMAND,
        /**
         * Learn end event.
         */
        LEARN_END,
    }

    /**
     * remote learn event listener
     */
    public interface RemoteLearnListener {
        /**
         * event handler
         *
         * @param event event that happened
         */
        void onEvent(Event event);
    }
}
