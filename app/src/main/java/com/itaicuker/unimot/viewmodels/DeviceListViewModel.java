package com.itaicuker.unimot.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.itaicuker.unimot.models.Device;
import com.itaicuker.unimot.repositories.DeviceRepository;

import java.util.List;

public class DeviceListViewModel extends ViewModel {
    LiveData<List<Device>> deviceListMutableLiveData;
    DeviceRepository repository;

    public DeviceListViewModel() {
        repository = new DeviceRepository();
        deviceListMutableLiveData = repository.getDeviceListMutableLiveData();
    }

    public LiveData<List<Device>> getDeviceListMutableLiveData() {
        return deviceListMutableLiveData;
    }
}
