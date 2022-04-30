package com.itaicuker.unimot.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.itaicuker.unimot.models.Device;
import com.itaicuker.unimot.repositories.Repository;

import java.util.List;

public class DeviceListViewModel extends ViewModel {
    LiveData<List<Device>> deviceListMutableLiveData;
    Repository repository;

    public DeviceListViewModel() {
        repository = new Repository();
        deviceListMutableLiveData = repository.getDeviceListLiveData();
    }

    public LiveData<List<Device>> getDeviceListMutableLiveData() {
        return deviceListMutableLiveData;
    }
}
