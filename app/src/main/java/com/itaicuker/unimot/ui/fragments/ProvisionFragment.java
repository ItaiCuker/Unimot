package com.itaicuker.unimot.ui.fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.espressif.provisioning.DeviceConnectionEvent;
import com.espressif.provisioning.ESPConstants;
import com.espressif.provisioning.ESPProvisionManager;
import com.itaicuker.unimot.R;
import com.itaicuker.unimot.databinding.FragmentProvisionBinding;
import com.itaicuker.unimot.ui.adapters.BleRemoteListAdapter;
import com.itaicuker.unimot.ui.models.Remote;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;


public class ProvisionFragment extends Fragment {

    private final String TAG = "ProvisionLandingFragment";
    
    //ui
    private NavController navController;
    private FragmentProvisionBinding binding;

    private Button btnScan;
    private ListView listRemotes;
    private ProgressBar progressBar;

    //prefix to filter BLE devices to only unimot
    private final String Prefix = "UNIMOT_";

    // Request codes
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_FINE_LOCATION = 2;

    private BluetoothAdapter bleAdapter;

    private BleRemoteListAdapter adapter;
    private ArrayList<Remote> remoteList;
    private Handler handler;
    private HashMap<BluetoothDevice, String> bluetoothRemotes;

    private boolean isDeviceConnected = false, isConnecting = false;

    private ESPProvisionManager provisionManager;

    private boolean isScanning = false;
    private boolean isRemoteConnected;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //using binding instead of findviewbyid
        binding = FragmentProvisionBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);    //line to override options menu.
//        requireActivity().getOnBackPressedDispatcher().addCallback(() ->
//        {
//
//        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //getting navController
        navController = Navigation.findNavController(view);

        // Checks if Bluetooth LE is supported on the device.
        if (!requireActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(requireActivity(), R.string.error_ble_not_supported, Toast.LENGTH_LONG).show();
            navController.navigateUp();
        }
        
        final BluetoothManager bluetoothManager = (BluetoothManager) requireActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        bleAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (bleAdapter == null) {
            Toast.makeText(requireActivity(), R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            navController.navigateUp();
        }

        isConnecting = false;
        isRemoteConnected = false;
        handler = new Handler();
        bluetoothRemotes = new HashMap<>();
        remoteList = new ArrayList<>();

        provisionManager = ESPProvisionManager.getInstance(requireActivity().getApplicationContext());

        initViews();

        EventBus.getDefault().register(this);

        final NavBackStackEntry dialogWifiEntry = navController.getBackStackEntry(R.id.action_provisionFragment_to_provisionWifiDialogFragment);

        // Create our observer and add it to the NavBackStackEntry's lifecycle
        final LifecycleEventObserver observer = new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event.equals(Lifecycle.Event.ON_DESTROY)
                        && dialogWifiEntry.getSavedStateHandle().contains("SSID")) {
                    String result = dialogWifiEntry.getSavedStateHandle().get("key");
                    // Do something with the result
                }
            }
        };
        dialogWifiEntry.getLifecycle().addObserver(observer);

        // As addObserver() does not automatically remove the observer, we
        // call removeObserver() manually when the view lifecycle is destroyed
        getViewLifecycleOwner().getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
            if (event.equals(Lifecycle.Event.ON_DESTROY)) {
                dialogWifiEntry.getLifecycle().removeObserver(this);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.provision_menu, menu);  //changing menu
    }

    private void initViews()
    {
        btnScan = binding.btnScan;
        listRemotes = binding.listRemotes;
        progressBar = binding.progressBar;

        adapter = new BleRemoteListAdapter(getContext(), R.layout.item_ble_scan, remoteList);

        listRemotes.setAdapter(adapter);
        listRemotes.setOnItemClickListener(onRemoteCLickListener);
    }

    private AdapterView.OnItemClickListener onRemoteCLickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

            stopScan();
            isConnecting = true;
            isDeviceConnected = false;
            btnScan.setVisibility(View.GONE);
            listRemotes.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            BLEProvisionLanding.this.position = position;
            Remote remote = adapter.getItem(position);
            String uuid = bluetoothRemotes.get(remote.getBluetoothDevice());
            Log.d(TAG, "=================== Connect to device : " + remote.getName() + " UUID : " + uuid);

            if (ActivityCompat.checkSelfPermission(BLEProvisionLanding.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                provisionManager.getEspDevice().connectBLEDevice(remote.getBluetoothDevice(), uuid);
                handler.postDelayed(disconnectDeviceTask, DEVICE_CONNECT_TIMEOUT);
            } else {
                Log.e(TAG, "Not able to connect device as Location permission is not granted.");
                Toast.makeText(BLEProvisionLanding.this, "Please give location permission to connect device", Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home)  //when user pressed back button
        {
            Log.d(TAG, "onOptionsItemSelected:home");
            if (isScanning) {
                stopScan();
            }
            if (provisionManager.getEspDevice() != null) {
                provisionManager.getEspDevice().disconnectDevice();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!bleAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {

            if (!isDeviceConnected && !isConnecting) {
                startScan();
            }
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult, requestCode : " + requestCode + ", resultCode : " + resultCode);

        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            navController.navigateUp();
            return;
        }

        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            startScan();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startScan();
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    navController.navigateUp();
                }
            }
            break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DeviceConnectionEvent event) {

        Log.d(TAG, "ON Device Prov Event RECEIVED : " + event.getEventType());
        handler.removeCallbacks(disconnectRemoteTask);

        switch (event.getEventType()) {

            case ESPConstants.EVENT_DEVICE_CONNECTED:
                Log.d(TAG, "Device Connected Event Received");
                ArrayList<String> deviceCaps = provisionManager.getEspDevice().getDeviceCapabilities();
                progressBar.setVisibility(View.GONE);
                isConnecting = false;
                isDeviceConnected = true;
                MutableLiveData<String> liveData = navController.getCurrentBackStackEntry().getSavedStateHandle().getLiveData("EVENT")
                break;

            case ESPConstants.EVENT_DEVICE_DISCONNECTED:

                progressBar.setVisibility(View.GONE);
                isConnecting = false;
                isDeviceConnected = false;
                Toast.makeText(BLEProvisionLanding.this, "Device disconnected", Toast.LENGTH_LONG).show();
                break;

            case ESPConstants.EVENT_DEVICE_CONNECTION_FAILED:
                progressBar.setVisibility(View.GONE);
                isConnecting = false;
                isDeviceConnected = false;
                alertForDeviceNotSupported("Failed to connect with device");
                break;
        }
    }

    private void startScan() {

        if (!hasPermissions() || isScanning) {
            return;
        }

        isScanning = true;
        deviceList.clear();
        bluetoothDevices.clear();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            provisionManager.searchBleEspDevices(deviceNamePrefix, bleScanListener);
            updateProgressAndScanBtn();
        } else {
            Log.e(TAG, "Not able to start scan as Location permission is not granted.");
            Toast.makeText(BLEProvisionLanding.this, "Please give location permission to start BLE scan", Toast.LENGTH_LONG).show();
        }
    }

}