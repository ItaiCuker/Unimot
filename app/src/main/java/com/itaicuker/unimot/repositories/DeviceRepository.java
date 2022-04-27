package com.itaicuker.unimot.repositories;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class DeviceRepository {

    private static final String TAG = "DeviceRepository";

    public static final FirebaseFirestore mFirestore =
            FirebaseFirestore.getInstance();
    public static final CollectionReference deviceCollection =
            mFirestore.collection("devices");
    public static final Query deviceQuery =
            deviceCollection.orderBy("timestamp", Query.Direction.DESCENDING);

    public DeviceRepository(){
    }

    public static Query getQuery() {
        return deviceQuery;
    }

    //TODO: delete a device
    //TODO: create a device
    //TODO: edit device
}