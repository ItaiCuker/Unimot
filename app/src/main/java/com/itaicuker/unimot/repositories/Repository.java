package com.itaicuker.unimot.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.itaicuker.unimot.models.Device;
import com.itaicuker.unimot.models.DeviceType;
import com.itaicuker.unimot.models.Remote;

import java.util.ArrayList;
import java.util.List;

public class Repository{

    private static final String TAG = "Repository";

    private static boolean init = false;

    private static FirebaseFirestore mFirestore;
    private static CollectionReference deviceCollection;
    private static CollectionReference remoteCollection;

    private static MutableLiveData<List<Device>> deviceListMutableLiveData;
    private static MutableLiveData<List<Remote>> remoteListMutableLiveData;
    private static MutableLiveData<Device> deviceMutableLiveData;

    private static ListenerRegistration deviceCollectionReg;
    private static ListenerRegistration remoteCollectionReg;
    private static ListenerRegistration singleDeviceReg;

    /**
     * singleton getter
     */
    public Repository()
    {
        if (!init) {
            mFirestore = FirebaseFirestore.getInstance();
            deviceCollection = mFirestore.collection("devices");
            remoteCollection = mFirestore.collection("remotes");
            init = true;
        }
    }

    /**
     * get singleton device list live data
     * @return device list LiveData
     */
    public LiveData<List<Device>> getDeviceListLiveData() {
        if (deviceListMutableLiveData == null) {
            deviceListMutableLiveData = new MutableLiveData<>();

            deviceCollectionReg = deviceCollection.addSnapshotListener(deviceCollectionSnapshotListener);
        }
        return deviceListMutableLiveData;
    }

    /**
     * get singleton remote list live data
     * @return remote list live data
     */
    public LiveData<List<Remote>> getRemoteListLiveData() {
        if (remoteListMutableLiveData == null) {
            remoteListMutableLiveData = new MutableLiveData<>();
            remoteCollectionReg = remoteCollection.addSnapshotListener(remoteCollectionSnapshotListener);
        }
        return remoteListMutableLiveData;
    }

    /**
     *
     * @param uId unique id of device in DB
     * @return device LiveData
     */
    public LiveData<Device> getDeviceLiveData(String uId) {
        if (deviceMutableLiveData == null) {
            deviceMutableLiveData = new MutableLiveData<>();
            singleDeviceReg = deviceCollection.document(uId).addSnapshotListener(singleDeviceSnapshotListener);
        }
        return deviceMutableLiveData;
    }

    /**
     * listener to devices collection
     */
    private final EventListener<QuerySnapshot> deviceCollectionSnapshotListener = (value, error) -> {
        if (value != null) {
            List<Device> deviceList = new ArrayList<>();
            for (QueryDocumentSnapshot doc : value){
                if (doc != null)
                    deviceList.add(docToDevice(doc));
            }
            deviceListMutableLiveData.postValue(deviceList);
        }
        else
            Log.e(TAG, error.toString());
    };

    /**
     * listener to remotes collection
     */
    private final EventListener<QuerySnapshot> remoteCollectionSnapshotListener = (value, error) -> {
        if (value != null) {
            List<Remote> remoteList = new ArrayList<>();
            for (QueryDocumentSnapshot doc : value) {
                if (doc != null)
                    remoteList.add(doc.toObject(Remote.class));
            }
            remoteListMutableLiveData.postValue(remoteList);
        } else
            Log.e(TAG, error.toString());
    };

    /**
     * listener to single device
     */
    private final EventListener<DocumentSnapshot> singleDeviceSnapshotListener = (doc, error) -> {
        if (doc != null){
            deviceMutableLiveData.postValue(docToDevice(doc));
        }
        else
            Log.e(TAG, error.toString());
    };

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