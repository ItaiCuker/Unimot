package com.itaicuker.unimot.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.itaicuker.unimot.models.Device;
import com.itaicuker.unimot.repositories.Repository;

public class DeviceViewModel extends ViewModel {

    private static final String TAG = "DeviceViewModel";
    private LiveData<Device> deviceMutableLiveData;
    private Repository repository;

    public DeviceViewModel() {
        repository = new Repository();
    }

    public LiveData<Device> getDeviceMutableLiveData(String uId) {
        if (deviceMutableLiveData == null)  //first time
            deviceMutableLiveData = repository.getDeviceLiveData(uId);
        return deviceMutableLiveData;
    }
}
