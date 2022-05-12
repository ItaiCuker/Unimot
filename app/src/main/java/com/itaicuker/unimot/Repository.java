package com.itaicuker.unimot;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.itaicuker.unimot.models.Device;
import com.itaicuker.unimot.models.DeviceType;
import com.itaicuker.unimot.models.Remote;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Repository.
 */
public class Repository {

    private static final String TAG = "UNIMOT: " + Repository.class.getSimpleName();
    /**
     * singleton self
     */
    private static Repository self = null;
    /**
     * The Activity.
     */
    final Activity activity;
    /**
     * The Commands collection.
     */
    final CollectionReference commandsCollection;

    /**
     * The Functions reference.
     */
    final FirebaseFunctions functions;
    /**
     * The Device collection.
     */
    final CollectionReference deviceCollection;
    /**
     * The Remote collection.
     */
    final CollectionReference remoteCollection;
    /**
     * listener single remote
     */
    private final EventListener<DocumentSnapshot> remoteSnapshotListener = (doc, error) -> {
        if (doc != null) {
            String learningState = doc.getString("state");
            Log.d(TAG, "state: " + learningState);
            switch (learningState) {
                case "start":
                    remoteLearnListener.onEvent(Remote.Event.LEARN_START);
                    break;
                case "gotCode":
                    remoteLearnListener.onEvent(Remote.Event.LEARN_REMOTE_RECEIVED_COMMAND);
                    break;
                case "tested":
                    remoteLearnListener.onEvent(Remote.Event.LEARN_REMOTE_TESTED_COMMAND);
                    break;
                case "":
                    if (isListenStart)
                        remoteLearnListener.onEvent(Remote.Event.LEARN_END);
                    else
                        isListenStart = true;
                    break;
            }
        } else
            Log.e(TAG, error.toString());
    };
    /**
     * The Remote list mutable live data.
     */
    MutableLiveData<List<Remote>> remoteListMutableLiveData;
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
     * listener to devices collection
     */
    private final EventListener<QuerySnapshot> deviceCollectionSnapshotListener = (value, error) -> {
        if (value != null) {
            List<Device> deviceList = new ArrayList<>();
            for (QueryDocumentSnapshot doc : value) {
                if (doc.exists())
                    deviceList.add(new Device(
                            doc.getId(),
                            doc.getString("name"),
                            DeviceType.valueOf(doc.getString("deviceType").toUpperCase()),
                            doc.getString("remoteId")
                    ));
                else
                    Log.w(TAG, "doc doesn't exist");
            }
            deviceListMutableLiveData.postValue(deviceList);
        } else
            Log.e(TAG, error.toString());
    };
    /**
     * The Device mutable live data.
     */
    MutableLiveData<Device> deviceMutableLiveData;
    /**
     * listener to device
     */
    private final EventListener<DocumentSnapshot> deviceSnapshotListener = (doc, error) -> {
        if (doc != null)
            deviceMutableLiveData.postValue(parseToDevice(doc));
        else
            Log.e(TAG, error.toString());
    };
    /**
     * The Device registration.
     */
    ListenerRegistration deviceReg;
    /**
     * The Device document.
     */
    DocumentReference deviceDoc;
    /**
     * The Remote registration.
     */
    ListenerRegistration remoteReg;
    /**
     * The Is listen start for elimination of first listen invoke.
     */
    boolean isListenStart;
    /**
     * The Remote learn listener.
     */
    Remote.RemoteLearnListener remoteLearnListener;
    /**
     * The Device list mutable live data.
     */
    MutableLiveData<List<Device>> deviceListMutableLiveData;


    /**
     * singleton getter
     *
     * @param activity main activity of app
     */
    private Repository(@NonNull Activity activity) {
        this.activity = activity;

        //getting db and collections
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        deviceCollection = db.collection("devices");
        remoteCollection = db.collection("remotes");
        commandsCollection = db.collection("commands");

        //getting functions instance
        functions = FirebaseFunctions.getInstance("europe-west1");

        //setting snapshot listeners bound to activity lifecycle
        deviceCollection.addSnapshotListener(activity, deviceCollectionSnapshotListener);
        remoteCollection.addSnapshotListener(activity, remoteCollectionSnapshotListener);

        //Init live data
        deviceListMutableLiveData = new MutableLiveData<>();
        remoteListMutableLiveData = new MutableLiveData<>();
    }

    /**
     * Create instance repository.
     *
     * @param activity the activity
     * @return the repository
     */
    public static Repository createInstance(@NonNull Activity activity) {
        if (self == null)
            self = new Repository(activity);
        return self;
    }

    /**
     * singleton getter
     *
     * @return instance of Repository
     */
    public static Repository getInstance() {
        return self;
    }

    /**
     * get singleton device list live data
     *
     * @return device list LiveData
     */
    public LiveData<List<Device>> getDeviceListLiveData() {
        return deviceListMutableLiveData;
    }

    /**
     * get singleton remote list live data
     *
     * @return remote list live data
     */
    public LiveData<List<Remote>> getRemoteListLiveData() {
        return remoteListMutableLiveData;
    }

    /**
     * Gets device live data.
     *
     * @param id of device in DB
     * @return device live data
     */
    public MutableLiveData<Device> getDeviceLiveData(@NonNull String id) {
        if (deviceMutableLiveData == null || deviceMutableLiveData.getValue() == null)
            deviceMutableLiveData = new MutableLiveData<>();

        deviceDoc = deviceCollection.document(id);
        deviceReg = deviceDoc.addSnapshotListener(deviceSnapshotListener);
        return deviceMutableLiveData;
    }

    /**
     * stop listening device
     */
    public void stopListeningDevice() {
        if (deviceReg != null)
            deviceReg.remove();
    }

    /**
     * start listening remote
     *
     * @param id                  remote id
     * @param remoteLearnListener listener for remote learn events
     */
    public void startListeningRemote(@NonNull String id, Remote.RemoteLearnListener remoteLearnListener) {
        this.remoteLearnListener = remoteLearnListener;
        isListenStart = false;
        remoteReg = remoteCollection.document(id).addSnapshotListener(remoteSnapshotListener);
    }

    /**
     * stop listening remote
     */
    public void stopListeningRemote() {
        if (remoteReg != null)
            remoteReg.remove();
    }

    /**
     * create a device in db
     *
     * @param map with device fields to create.
     */
    public void createDevice(@NonNull Map map) {
        if (!deviceExists(map))
            deviceCollection.add(map)
                    .addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot created with ID: " + documentReference.getId()))
                    .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
        else
            Toast.makeText(activity, R.string.device_exists, Toast.LENGTH_LONG).show();
    }

    /**
     * edit device
     *
     * @param map with device fields
     */
    public void editDevice(@NonNull Map map) {
        if (!deviceExists(map))
            deviceDoc.set(map)
                    .addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot edited"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error editing document", e));
        else
            Toast.makeText(activity, R.string.device_exists, Toast.LENGTH_LONG).show();
    }

    /**
     * delete a device
     *
     * @param commands the commands map of device
     */
    public void deleteDevice(@NonNull Map<String, String> commands) {
        //deleting all commands created by device.
        commands.values().forEach(commandId -> commandsCollection.document(commandId).delete());
        //deleting device
        deviceDoc.delete();
        //also deleting device from deviceListMutableLiveData
        List<Device> deviceList = deviceListMutableLiveData.getValue();
        deviceList.removeIf(device -> device.getId() == deviceDoc.getId());
        deviceListMutableLiveData.setValue(deviceList);
    }

    /**
     * @param map of device fields
     * @return true if device exists in deviceListMutableLiveData
     */
    private boolean deviceExists(@NonNull Map map) {
        return deviceListMutableLiveData.getValue()
                .stream()
                .anyMatch(device -> compareDeviceToMap(device, map));
    }

    /**
     * Compare device to map.
     *
     * @param device to check
     * @param map    to check
     * @return true if device has the same fields as map, false otherwise
     */
    public boolean compareDeviceToMap(@NonNull Device device, @NonNull Map map) {
        return device.getDeviceType().name().equals(map.get("deviceType")) &&
                device.getName().equals(map.get("name")) &&
                device.getRemoteId().equals(map.get("remoteId"));
    }

    /**
     * @param doc Firestore document
     * @return parsed document Device, null if document doesn't exist
     */
    @Nullable
    private Device parseToDevice(@NonNull DocumentSnapshot doc) {
        Device device = null;

        if (doc.exists()) {
            device = new Device(
                    doc.getId(),
                    doc.getString("name"),
                    DeviceType.valueOf(doc.getString("deviceType").toUpperCase()),
                    doc.getString("remoteId"),
                    doc.getBoolean("isAvailable"),
                    //if device contains commands map return it, else return empty map.
                    (Map<String, String>) (doc.contains("commands") ? doc.get("commands") : new HashMap<String, String>())
            );
        }
        return device;
    }

    /**
     * send command to remote
     *
     * @param obj JSON object with request
     */
    public void sendCommand(JSONObject obj) {
        functions.getHttpsCallable("sendCommand").call(obj)
                .addOnFailureListener(e -> {    //on fail start learn
                    Log.e(TAG, "sendCommand: " + e.toString());
                    e.printStackTrace();
                    if (remoteLearnListener != null)
                        remoteLearnListener.onEvent(Remote.Event.LEARN_END);
                });
    }

    /**
     * delete command document and reference in device
     *
     * @param commandId   the command id
     * @param commandName the command name
     */
    public void deleteCommand(String commandId, String commandName) {
        //deleting map field in device
        deviceDoc.update("commands." + commandName, FieldValue.delete());
        //deleting command document
        commandsCollection.document(commandId).delete();
    }
}