package com.itaicuker.unimot.ui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.espressif.provisioning.DeviceConnectionEvent;
import com.espressif.provisioning.ESPConstants;
import com.espressif.provisioning.ESPProvisionManager;
import com.espressif.provisioning.listeners.BleScanListener;
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

    //ui declarations
    private NavController navController;
    private FragmentProvisionBinding binding;

    private Button btnScan;
    private ListView lvRemotes;
    private ProgressBar progressBar;

    //prefix to filter BLE devices to only unimot
    private final String prefix = "UNIMOT_";

    // Request codes
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_FINE_LOCATION = 2;

    private BluetoothAdapter bleAdapter;

    private BleRemoteListAdapter adapter;
    private ArrayList<Remote> remoteList;
    private int position;
    private Handler handler;
    private HashMap<BluetoothDevice, String> bluetoothDevices;

    private boolean isRemoteConnected = false, isConnecting = false, isScanning = false;

    private ESPProvisionManager provisionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //using binding instead of findviewbyid
        binding = FragmentProvisionBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);    //line to override options menu.
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //getting navController
        navController = Navigation.findNavController(view);

        // Checks if Bluetooth LE is supported on this device.
        if (!requireActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(getActivity(), R.string.error_ble_not_supported, Toast.LENGTH_LONG).show();
            navController.navigateUp();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        bleAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on this device.
        if (bleAdapter == null) {
            Toast.makeText(getActivity(), R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            navController.navigateUp();
        }

        isConnecting = false;
        isRemoteConnected = false;
        handler = new Handler();
        bluetoothDevices = new HashMap<>();
        remoteList = new ArrayList<>();

        provisionManager = ESPProvisionManager.getInstance(getActivity().getApplicationContext());

        initViews();

        EventBus.getDefault().register(this);
    }

    private void initViews() {
        btnScan = binding.btnScan;
        btnScan.setOnClickListener(btnScanClickListener);

        lvRemotes = binding.listRemotes;
        progressBar = binding.progressBar;

        adapter = new BleRemoteListAdapter(getContext(), R.layout.item_ble_scan, remoteList);

        lvRemotes.setAdapter(adapter);
        lvRemotes.setOnItemClickListener(onRemoteCLickListener);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on this device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!bleAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {

            if (!isRemoteConnected && !isConnecting) {
                startScan();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.provision_menu, menu);  //changing menu
    }

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
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult, requestCode : " + requestCode + ", resultCode : " + resultCode);

        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getActivity(), "Provisioning requires bluetooth", Toast.LENGTH_LONG).show();
                navController.navigateUp();
            }
            if (requestCode == Activity.RESULT_OK)
                startScan();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_FINE_LOCATION)
        {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                startScan();
            else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED)
            {
                Toast.makeText(getActivity(), "Provisioning requires location permission", Toast.LENGTH_LONG);
                navController.navigateUp();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DeviceConnectionEvent event) {

        Log.d(TAG, "ON Device Prov Event RECEIVED : " + event.getEventType());
        switch (event.getEventType()) {

            case ESPConstants.EVENT_DEVICE_CONNECTED:
                Log.d(TAG, "Device Connected Event Received");
                ArrayList<String> remoteCaps = provisionManager.getEspDevice().getDeviceCapabilities();
                progressBar.setVisibility(View.GONE);
                isConnecting = false;
                isRemoteConnected = true;

                //TODO: start WiFi credentials dialog and get result.

                break;

            case ESPConstants.EVENT_DEVICE_DISCONNECTED:

                progressBar.setVisibility(View.GONE);
                isConnecting = false;
                isRemoteConnected = false;
                Toast.makeText(getContext(), "Remote disconnected", Toast.LENGTH_LONG).show();
                break;

            case ESPConstants.EVENT_DEVICE_CONNECTION_FAILED:
                progressBar.setVisibility(View.GONE);
                isConnecting = false;
                isRemoteConnected = false;
                alertForRemoteNotSupported("Failed to connect to remote");
                break;
        }
    }

    private final View.OnClickListener btnScanClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            bluetoothDevices.clear();
            adapter.clear();
            startScan();
        }
    };

    @SuppressLint("MissingPermission")
    private void startScan() {

        if (!hasPermissions() || isScanning) {
            return;
        }

        isScanning = true;
        remoteList.clear();
        bluetoothDevices.clear();

        if (hasLocationPermissions()) {

            provisionManager.searchBleEspDevices(prefix, bleScanListener);
            updateProgressAndScanBtn();
        } else {
            Log.e(TAG, "Not able to start scan as Location permission is not granted.");
            Toast.makeText(getActivity(), "Please give location permission to start BLE scan", Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void stopScan() {

        isScanning = false;

        if (hasLocationPermissions()) {
            provisionManager.stopBleScan();
            updateProgressAndScanBtn();
        } else {
            Log.e(TAG, "Not able to stop scan as Location permission is not granted.");
            Toast.makeText(getActivity(), "Please give location permission to stop BLE scan", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * This method will update UI (Scan button enable / disable and progressbar visibility)
     */
    private void updateProgressAndScanBtn() {

        if (isScanning) {

            btnScan.setEnabled(false);
            btnScan.setAlpha(0.5f);
            btnScan.setTextColor(Color.WHITE);
            progressBar.setVisibility(View.VISIBLE);
            lvRemotes.setVisibility(View.GONE);

        } else {

            btnScan.setEnabled(true);
            btnScan.setAlpha(1f);
            progressBar.setVisibility(View.GONE);
            lvRemotes.setVisibility(View.VISIBLE);
        }
    }

    /**
     * alert dialog that shows error and closes fragment on positive button
     * @param msg error to show in message field of AlertDialog
     */
    private void alertForRemoteNotSupported(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);

        builder.setTitle(R.string.error_title);
        builder.setMessage(msg);

        // Set up the buttons
        builder.setPositiveButton(R.string.btn_ok, (dialog, which) -> navController.navigateUp());

        builder.show();
    }

    /**
     * checks and requests required permissions of fragment
     * @return true fragment already has permissions, false if requesting permissions.
     */
    private boolean hasPermissions() {

        if (bleAdapter == null || !bleAdapter.isEnabled()) {

            requestBluetoothEnable();
            return false;

        } else if (!hasLocationPermissions()) {

            requestLocationPermission();
            return false;
        }
        return true;
    }

    /**
     * requesting to enable bluetooth via Intent.
     */
    private void requestBluetoothEnable() {

        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        Log.d(TAG, "Requested user enables Bluetooth.");
    }

    /**
     * checks for ACCESS_FINE_LOCATION granted
     * @return returns boolean if permission granted
     */
    private boolean hasLocationPermissions() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * requesting permission ACCESS_FINE_LOCATION
     */
    private void requestLocationPermission() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
    }

    private BleScanListener bleScanListener = new BleScanListener() {

        @Override
        public void scanStartFailed() {
            Toast.makeText(getActivity(), "Please turn on Bluetooth to connect BLE device", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPeripheralFound(BluetoothDevice device, ScanResult scanResult) {

            Log.d(TAG, "====== onPeripheralFound ===== " + device.getName());
            boolean deviceExists = false;
            String serviceUuid = "";

            if (scanResult.getScanRecord().getServiceUuids() != null && scanResult.getScanRecord().getServiceUuids().size() > 0) {
                serviceUuid = scanResult.getScanRecord().getServiceUuids().get(0).toString();
            }
            Log.d(TAG, "Add service UUID : " + serviceUuid);

            if (bluetoothDevices.containsKey(device)) {
                deviceExists = true;
            }

            if (!deviceExists) {
                Remote remote = new Remote();
                remote.setName(scanResult.getScanRecord().getDeviceName());
                remote.setBluetoothDevice(device);

                lvRemotes.setVisibility(View.VISIBLE);
                bluetoothDevices.put(device, serviceUuid);
                remoteList.add(remote);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void scanCompleted() {
            isScanning = false;
            updateProgressAndScanBtn();
        }

        @Override
        public void onFailure(Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    };

    private final AdapterView.OnItemClickListener onRemoteCLickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            //stopping scan
            stopScan();
            //setting state booleans
            isConnecting = true;
            isRemoteConnected = false;
            //setting visibility
            btnScan.setVisibility(View.GONE);
            lvRemotes.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            ProvisionFragment.this.position = position;
            Remote remote = adapter.getItem(position);
            String uuid = bluetoothDevices.get(remote.getBluetoothDevice());
            Log.d(TAG, "=================== Connect to remote : " + remote.getName() + " UUID : " + uuid);

            if (hasLocationPermissions()) {
                provisionManager.getEspDevice().connectBLEDevice(remote.getBluetoothDevice(), uuid);
            } else {
                Log.e(TAG, "Not able to connect remote as Location permission is not granted.");
                Toast.makeText(getActivity(), R.string.permmision_location_request, Toast.LENGTH_LONG).show();
            }
        }
    };
}