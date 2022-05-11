package com.itaicuker.unimot.viewModels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.itaicuker.unimot.Repository;
import com.itaicuker.unimot.models.Device;

import org.json.JSONObject;

/**
 * The type Device view model.
 */
public class DeviceViewModel extends ViewModel {

    private static final String TAG = "UNIMOT: " + "UNIMOT: " + DeviceViewModel.class.getSimpleName();
    private MutableLiveData<Device> deviceMutableLiveData;
    private final Repository repository;

    /**
     * Instantiates a new Device view model.
     */
    public DeviceViewModel() {
        repository = Repository.getInstance();
    }

    /**
     * get new live data from repository
     *
     * @param uId id of device
     * @return live data of device
     */
    public LiveData<Device> getDeviceMutableLiveData(String uId) {
        deviceMutableLiveData = repository.getDeviceLiveData(uId);
        Log.d(TAG, String.valueOf(deviceMutableLiveData.getValue()));
        return deviceMutableLiveData;
    }

    /**
     * getter for device live data
     *
     * @return device live data
     */
    public LiveData<Device> getDeviceMutableLiveData() {
        return deviceMutableLiveData;
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "onCleared");
        repository.stopListeningDevice();
    }

    /**
     * Delete.
     */
    public void delete() {
        repository.deleteDevice(deviceMutableLiveData.getValue().getId(), deviceMutableLiveData.getValue().getCommands());
        deviceMutableLiveData.postValue(null);
    }

    /**
     * Delete command.
     *
     * @param commandId   the command id
     * @param commandName
     */
    public void deleteCommand(String commandId, String commandName) {
        repository.deleteCommand(commandId, commandName, deviceMutableLiveData.getValue().getId());
    }

    /**
     * send command to remote
     *
     * @param obj data request
     */
    public void sendCommand(JSONObject obj) {
        repository.sendCommand(obj);
    }
}
