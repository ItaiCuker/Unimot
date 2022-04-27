package com.itaicuker.unimot.repositories;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.itaicuker.unimot.models.Device;
import com.itaicuker.unimot.models.DeviceType;

import java.util.ArrayList;
import java.util.List;

public class DeviceListRepository {

    private static final String TAG = "DeviceListRepository";

    MutableLiveData<List<Device>> deviceListMutableLiveData;
    FirebaseFirestore mFirestore;
    MutableLiveData<Device> deviceMutableLiveData;

    public DeviceListRepository(){
        this.deviceListMutableLiveData = new MutableLiveData<>();
        //define firestore
        mFirestore = FirebaseFirestore.getInstance();
        //define deviceList
        deviceMutableLiveData = new MutableLiveData<>();
    }

    /**
     * creates device list from Firestore DB and returns it
     * @return mutable live data that encapsulates device list
     */
    public MutableLiveData<List<Device>> getDeviceListMutableLiveData() {
        Log.i(TAG, "getDeviceListMutableLiveData: ");
//        mFirestore.collection("Device").addSnapshotListener((value, error) -> {
//            List<Device> blogList = new ArrayList<>();
//            for (QueryDocumentSnapshot doc : value) {
//                if (doc != null)
//                    blogList.add(doc.toObject(Device.class));
//            }
//            deviceListMutableLiveData.postValue(blogList);
//        });
        List<Device> deviceList = new ArrayList<>();
        deviceList.add(new Device("merry's A/C", "1", true, DeviceType.AC));
        deviceList.add(new Device("yodan's TV", "2", false, DeviceType.TV));
        deviceList.add(new Device("guy's projector", "2", false, DeviceType.PROJECTOR));
        deviceList.add(new Device("michals's Speaker", "2", false, DeviceType.SPEAKER));
        deviceListMutableLiveData.postValue(deviceList);
        return deviceListMutableLiveData;
    }

    //TODO: delete a device
    //TODO: create a device
    //TODO: change button config of device
}
