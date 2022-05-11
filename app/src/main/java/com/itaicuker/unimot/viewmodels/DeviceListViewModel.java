package com.itaicuker.unimot.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.itaicuker.unimot.Repository;
import com.itaicuker.unimot.models.Device;

import java.util.List;

/**
 * The ViewModel Device list view model.
 */
public class DeviceListViewModel extends ViewModel {
    /**
     * The Device list mutable live data.
     */
    LiveData<List<Device>> deviceListMutableLiveData;
    /**
     * The Repository.
     */
    Repository repository;

    /**
     * Instantiates a new Device list view model.
     */
    public DeviceListViewModel() {
        repository = Repository.getInstance();
        deviceListMutableLiveData = repository.getDeviceListLiveData();
    }

    /**
     * Gets device list live data.
     *
     * @return the device list live data
     */
    public LiveData<List<Device>> getDeviceListLiveData() {
        return deviceListMutableLiveData;
    }
}
