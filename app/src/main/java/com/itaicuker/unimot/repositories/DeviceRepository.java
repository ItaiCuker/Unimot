package com.itaicuker.unimot.repositories;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.itaicuker.unimot.models.Device;
import com.itaicuker.unimot.models.DeviceType;

import java.util.ArrayList;
import java.util.List;

public class DeviceRepository implements EventListener<QuerySnapshot>{

    private static final String TAG = "DeviceRepository";

    private boolean init = false;

    private static FirebaseFirestore mFirestore = null;
    private static CollectionReference deviceCollection = null;
    private static Query deviceQuery = null;

    private static MutableLiveData<List<Device>> deviceListMutableLiveData;
    private MutableLiveData<Device> deviceMutableLiveData;

    /**
     * singleton getter
     */
    public DeviceRepository()
    {
        if (!init) {
            mFirestore = FirebaseFirestore.getInstance();
            deviceCollection = mFirestore.collection("devices");

            deviceQuery = deviceCollection.orderBy("name", Query.Direction.DESCENDING);
            deviceQuery.addSnapshotListener(this);

            deviceListMutableLiveData = new MutableLiveData<>();
        }
    }

    /**
     * @return device list LiveData
     */
    public LiveData<List<Device>> getDeviceListMutableLiveData() {
        return deviceListMutableLiveData;
    }

    /**
     *
     * @param uId unique id of device in DB
     * @return device LiveData
     */
    public LiveData<Device> getDeviceMutableLiveData(String uId){
        if (deviceMutableLiveData == null) {
            deviceMutableLiveData = new MutableLiveData<>();
            listenToSingleDevice(uId);
        }
        return deviceMutableLiveData;
    }

    /**
     * starting to listen to single device and posting it to LiveData
     * @param uId unique id of device in DB
     */
    private void listenToSingleDevice(String uId) {
        deviceCollection.document(uId).addSnapshotListener((doc, error) ->
        {//listener to single device
            if (doc != null){
                deviceMutableLiveData.postValue(docToDevice(doc));
            }
            else
                Log.e(TAG, error.toString());
        });
    }

    /**
     * listener to entire list of devices
     */
    @Override
    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

        if (value != null){
            List<Device> deviceList = new ArrayList<>();
            for (QueryDocumentSnapshot doc : value){
                if (doc != null)
                    deviceList.add(docToDevice(doc));
            }
            deviceListMutableLiveData.postValue(deviceList);
        }
        else
            Log.e(TAG, error.toString());
    }

    /**
     *
     * @param doc firestore document
     * @return parsed document in device form
     */
    private Device docToDevice(DocumentSnapshot doc){
         return new Device(
                 doc.getId(),
                 doc.getString("name"),
                 doc.get("deviceType", DeviceType.class)
         );
    }

    //TODO: delete a device
    //TODO: create a device
    //TODO: edit device
}