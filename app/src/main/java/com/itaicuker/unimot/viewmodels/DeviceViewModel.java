package com.itaicuker.unimot.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.itaicuker.unimot.models.Device;
import com.itaicuker.unimot.repositories.Repository;

public class DeviceViewModel extends ViewModel {

    private static final String TAG = "DeviceViewModel";
    private LiveData<Device> deviceLiveData;
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
        deviceLiveData = repository.getDeviceLiveData(uId);
        return deviceLiveData;
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        repository.stopListeningSingleDevice();
    }

    public void deleteDevice() {
        repository.deleteDevice(deviceLiveData.getValue().getuId());
    }
}
