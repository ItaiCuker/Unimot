package com.itaicuker.unimot.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.itaicuker.unimot.models.Device;
import com.itaicuker.unimot.repositories.DeviceListRepository;

import java.util.List;

public class DeviceListViewModel extends ViewModel {
    MutableLiveData<List<Device>> deviceListMutableLiveData;
    FirebaseFirestore mFirestore;
    DeviceListRepository deviceListRepository;

    public DeviceListViewModel() {
        deviceListRepository = new DeviceListRepository();  //creates new repository
        deviceListMutableLiveData = deviceListRepository.getDeviceListMutableLiveData();    //gets device list from repository
        mFirestore = FirebaseFirestore.getInstance();   //gets instance of firestore
    }

    /**
     *
     * @return mutable data device list
     */
    public MutableLiveData<List<Device>> getDeviceListMutableLiveData() {
        return deviceListMutableLiveData;
    }
}
