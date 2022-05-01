package com.itaicuker.unimot.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.itaicuker.unimot.models.Device;
import com.itaicuker.unimot.models.DeviceType;
import com.itaicuker.unimot.models.Remote;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Repository {
    private static Repository self = null;

    private static final String TAG = "Repository";

    final FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build();

    private static FirebaseFirestore db;
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
    private Repository()
    {
        db = FirebaseFirestore.getInstance();
        db.setFirestoreSettings(settings);
        deviceCollection = db.collection("devices");
        remoteCollection = db.collection("remotes");

        deviceListMutableLiveData = new MutableLiveData<>();
        remoteListMutableLiveData = new MutableLiveData<>();
    }

    public static Repository getInstance() {
        if (self == null)
            self = new Repository();
        return self;
    }

    /**
     * start listening
     */
    public void startListening() {
        deviceCollectionReg = deviceCollection.addSnapshotListener(deviceCollectionSnapshotListener);
        remoteCollectionReg = remoteCollection.addSnapshotListener(remoteCollectionSnapshotListener);
    }

    /**
     * stop listening
     */
    public void stopListening() {
        if (deviceCollectionReg != null)
            deviceCollectionReg.remove();
        if (remoteCollectionReg != null)
            remoteCollectionReg.remove();
        if (singleDeviceReg != null)
            singleDeviceReg.remove();
    }

    public void stopListeningSingleDevice() {
        if (singleDeviceReg != null)
            singleDeviceReg.remove();
    }

    /**
     * get singleton device list live data
     * @return device list LiveData
     */
    public LiveData<List<Device>> getDeviceListLiveData() {
        return deviceListMutableLiveData;
    }

    /**
     * get singleton remote list live data
     * @return remote list live data
     */
    public LiveData<List<Remote>> getRemoteListLiveData() {
        return remoteListMutableLiveData;
    }

    /**
     *
     * @param uId unique id of device in DB
     * @return device live data
     */
    public LiveData<Device> getDeviceLiveData(String uId) {
        if (deviceMutableLiveData == null)
            deviceMutableLiveData = new MutableLiveData<>();

        singleDeviceReg = deviceCollection.document(uId).addSnapshotListener(singleDeviceSnapshotListener);
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
                if (doc != null) {
                    remoteList.add(new Remote(doc.getId(), doc.getBoolean("isOnline")));
                }
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
                 DeviceType.valueOf(doc.getString("deviceType").toUpperCase()),
                 doc.getString("remoteId"),
                 doc.getBoolean("isOnline")
         );
    }

    /**
     * create a device in db
     * @param map with device fields to create.
     */
    public void createDevice(Map map) {
        deviceCollection.add(map)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot created with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
    }

    /**
     * edits devices fields (excluding commands)
     * @param map with device fields, can be the same.
     * @param uId UID of document in Firestore
     */
    public void editDevice(Map map, String uId) {
        deviceCollection.document(uId).set(map)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot edited with ID: " + uId))
                .addOnFailureListener(e -> Log.w(TAG, "Error editing document", e));
    }

    /**
     * delete a device
     * @param uId UID of document in Firestore
     */
    public void deleteDevice(String uId) {
        deviceCollection.document(uId).delete()
                .addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot deleted with ID: " + uId))
                .addOnFailureListener(e -> Log.w(TAG, "Error deleting document", e));
    }
}