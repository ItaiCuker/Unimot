package com.itaicuker.unimot.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.itaicuker.unimot.Repository;
import com.itaicuker.unimot.models.Device;

import java.util.List;

public class DeviceListViewModel extends ViewModel {
    LiveData<List<Device>> deviceListMutableLiveData;
    Repository repository;

    public DeviceListViewModel() {
        repository = Repository.getInstance();
        deviceListMutableLiveData = repository.getDeviceListLiveData();
    }

    public LiveData<List<Device>> getDeviceListLiveData() {
        return deviceListMutableLiveData;
    }
}
