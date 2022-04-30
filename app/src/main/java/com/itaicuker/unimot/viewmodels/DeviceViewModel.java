package com.itaicuker.unimot.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.itaicuker.unimot.models.Device;
import com.itaicuker.unimot.repositories.DeviceRepository;

public class DeviceViewModel extends ViewModel {

    private static final String TAG = "DeviceViewModel";
    private MutableLiveData<Device> deviceMutableLiveData;
    private DeviceRepository repository;

    public DeviceViewModel() {
        repository = new DeviceRepository();
    }

    public LiveData<Device> getDeviceMutableLiveData(String uId) {
        return repository.getDeviceMutableLiveData(uId);
    }
}
