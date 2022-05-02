package com.itaicuker.unimot.viewModels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.itaicuker.unimot.models.Device;
import com.itaicuker.unimot.repositories.Repository;

public class DeviceViewModel extends ViewModel {

    private static final String TAG = "DeviceViewModel";
    private MutableLiveData<Device> deviceMutableLiveData;
    private Repository repository;

    public DeviceViewModel() {
        repository = Repository.getInstance();
    }

    /**
     *
     * @param uId id of device
     * @return live data of device
     */
    public LiveData<Device> getDeviceMutableLiveData(String uId) {
        deviceMutableLiveData = repository.getDeviceLiveData(uId);
        Log.d(TAG, String.valueOf(deviceMutableLiveData.getValue()));
        return deviceMutableLiveData;
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "onCleared");
        repository.stopListeningSingleDevice();
    }

    public void deleteDevice() {
        repository.deleteDevice(deviceMutableLiveData.getValue().getuId());
        deviceMutableLiveData.postValue(null);
    }
}
