package com.itaicuker.unimot.repositories;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

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
import com.itaicuker.unimot.R;
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

    private final Context context;

    private final CollectionReference deviceCollection;
    private final CollectionReference remoteCollection;

    private MutableLiveData<List<Device>> deviceListMutableLiveData;
    private MutableLiveData<List<Remote>> remoteListMutableLiveData;
    private MutableLiveData<Device> deviceMutableLiveData;

    private ListenerRegistration deviceCollectionReg;
    private ListenerRegistration remoteCollectionReg;
    private ListenerRegistration singleDeviceReg;


    /**
     * singleton getter
     * @param context
     */
    private Repository(Context context)
    {
        this.context = context;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.setFirestoreSettings(settings);
        deviceCollection = db.collection("devices");
        remoteCollection = db.collection("remotes");

        deviceListMutableLiveData = new MutableLiveData<>();
        remoteListMutableLiveData = new MutableLiveData<>();
    }

    public static Repository createInstance(Context context) {
        if (self == null)
            self = new Repository(context);
        return self;
    }

    public static Repository getInstance() {
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
    public MutableLiveData<Device> getDeviceLiveData(String uId) {
        if (deviceMutableLiveData == null || deviceMutableLiveData.getValue() == null)
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
        if (doc != null)
            deviceMutableLiveData.postValue(docToDevice(doc));
        else
            Log.e(TAG, error.toString());
    };

    /**
     * create a device in db
     * @param map with device fields to create.
     */
    public void createDevice(Map map) {
        if (!deviceExists(map))
            deviceCollection.add(map)
                    .addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot created with ID: " + documentReference.getId()))
                    .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
        else
            Toast.makeText(context, R.string.device_exists, Toast.LENGTH_LONG).show();
    }

    /**
     * edits devices fields (excluding commands)
     * @param map with device fields, can be the same.
     * @param uId UID of document in Firestore
     */
    public void editDevice(Map map, String uId) {
        if (!deviceExists(map))
        deviceCollection.document(uId).set(map)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot edited with ID: " + uId))
                .addOnFailureListener(e -> Log.w(TAG, "Error editing document", e));
        else
            Toast.makeText(context, R.string.device_exists, Toast.LENGTH_LONG).show();
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

    /**
     *
     * @param map of device fields
     * @return true if device exists in deviceListMutableLiveData
     */
    private boolean deviceExists(Map map) {
        return deviceListMutableLiveData.getValue()
                .stream()
                .anyMatch(device -> isDeviceMap(device, map));
    }

    /**
     *
     * @param device to check
     * @param map to check
     * @return true if device has the same fields as map, false otherwise
     */
    public boolean isDeviceMap(Device device, Map map) {
        return  device.getDeviceType().name().equals(map.get("deviceType")) &&
                device.getName().equals(map.get("name")) &&
                device.getRemoteId().equals(map.get("remoteId"));
    }

    /**
     *
     * @param doc firestore document
     * @return parsed document Device, null if document doesn't exist
     */
    private Device docToDevice(DocumentSnapshot doc){
        Device device = null;
        if (doc.exists())
            device = new Device(
                    doc.getId(),
                    doc.getString("name"),
                    DeviceType.valueOf(doc.getString("deviceType").toUpperCase()),
                    doc.getString("remoteId"),
                    doc.getBoolean("isOnline")
            );
        else
            Log.w(TAG, "doc doesn't exist");
        return device;
    }
}