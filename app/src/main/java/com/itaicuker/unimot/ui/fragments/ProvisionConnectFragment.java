package com.itaicuker.unimot.ui.fragments;

import static com.itaicuker.unimot.ui.fragments.WifiCredentialsDialogFragment.REQUEST_WIFI_CREDENTIALS;

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
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
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


public class ProvisionConnectFragment extends Fragment {

    private final String TAG = "ProvisionConnectFragment";

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
    private static final int REQUEST_LOCATION = 3;

    private BluetoothAdapter bleAdapter;    //Bluetooth service adapter

    private BleRemoteListAdapter adapter;   //ListView adapter
    private ArrayList<Remote> remoteList;   //ArrayList to store scanned remotes data

    private HashMap<BluetoothDevice, String> bluetoothDevices; //hash map to bind BluetoothDevice obects with their respective UUID's

    private boolean isRemoteConnected = false, isConnecting = false, isScanning = false;    //booleans for fragment state

    private ESPProvisionManager provisionManager;   //manager singelton for using library

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

        //getting Bluetooth manager adapter
        final BluetoothManager bluetoothManager = (BluetoothManager) requireContext().getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        bleAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on this device.
        if (bleAdapter == null) {
            Toast.makeText(getActivity(), R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            navController.navigateUp();
        }

        //initializing state and sets
        isConnecting = false;
        isRemoteConnected = false;
        bluetoothDevices = new HashMap<>();
        remoteList = new ArrayList<>();

        //getting provisionManager and creating ESPDevice object
        provisionManager = ESPProvisionManager.getInstance(requireContext().getApplicationContext());
        //using BLE for communication, SECURITY_1 means its encrypted.
        provisionManager.createESPDevice(ESPConstants.TransportType.TRANSPORT_BLE, ESPConstants.SecurityType.SECURITY_1);

        initViews();

        EventBus.getDefault().register(this);   //registering event for provisionManager usage
    }

    /**
     * initializing views for layout
     */
    private void initViews() {
        btnScan = binding.btnScan;
        btnScan.setOnClickListener(btnScanClickListener);

        lvRemotes = binding.listRemotes;
        progressBar = binding.progressBar;

        adapter = new BleRemoteListAdapter(requireContext(), R.layout.item_ble_scan, remoteList);

        lvRemotes.setAdapter(adapter);
        lvRemotes.setOnItemClickListener(onRemoteCLickListener);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!isRemoteConnected && !isConnecting)    //starting scan if not started yet
                startScan();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.provision_menu, menu);  //changing options menu
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home)  //handling when user pressed back button
        {
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
        EventBus.getDefault().unregister(this); //unregistering event handler onDestroy fragment
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult, requestCode : " + requestCode + ", resultCode : " + resultCode);


        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {   // User chose not to enable Bluetooth.
                Toast.makeText(getActivity(), "Provisioning requires bluetooth", Toast.LENGTH_LONG).show();
                navController.navigateUp();
            }
            if (resultCode == Activity.RESULT_OK)   //User enabled Bluetooth
                startScan();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_FINE_LOCATION) //Location permission
        {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)    //permmision granted
                startScan();
            else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED)//permmision denied
            {
                Toast.makeText(getActivity(), R.string.toast_location_perm_denied, Toast.LENGTH_LONG).show();
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

                //navigating to dialog
                navController.navigate(R.id.action_global_provisionWifiDialogFragment);

                //waiting for result
                requireActivity().getSupportFragmentManager().setFragmentResultListener(REQUEST_WIFI_CREDENTIALS, getViewLifecycleOwner(), (requestKey, result) -> {
                    String SSID = result.getString("SSID", null);
                    String Pass = result.getString("Pass", null);
                });
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
                alertForRemoteNotSupported();
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

        if (!hasDependencies() || isScanning) { //checking for dependencies and if already scanning
            return;
        }

        isScanning = true;
        remoteList.clear();
        bluetoothDevices.clear();


        provisionManager.searchBleEspDevices(prefix, bleScanListener);
        updateProgressAndScanBtn();
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
     */
    private void alertForRemoteNotSupported() {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setCancelable(false)
               .setTitle(R.string.error_title)
               .setMessage("Failed to connect to remote")
               .setPositiveButton(R.string.btn_ok, (dialog, which) -> dialog.dismiss())
               .show();

        // Set up the buttons

    }

    /**
     * checks and requests required permissions of fragment
     * @return true fragment already has permissions, false if requesting permissions.
     */
    private boolean hasDependencies() {

        boolean flag = true;
        if (bleAdapter == null || !bleAdapter.isEnabled()) {    //bluetooth not enabled
            requestBluetoothEnable();
            flag = false;
        }
        if (!hasLocationPermissions()) { //location not permitted
            requestLocationPermission();
            flag = false;
        }
        if (!isLocationEnabled()) { //location no enabled
            askForLocation();
            flag = false;
        }
        return flag;
    }

    /**
     * requesting to enable bluetooth via Intent.
     */
    private void requestBluetoothEnable() {

        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        Log.d(TAG, "Requested user enables Bluetooth.");
    }

    private boolean isLocationEnabled() {

        boolean gps_enabled = false;
        boolean network_enabled = false;
        LocationManager lm = (LocationManager) requireActivity().getApplicationContext().getSystemService(Activity.LOCATION_SERVICE);

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        Log.d(TAG, "GPS Enabled : " + gps_enabled + " , Network Enabled : " + network_enabled);

        return gps_enabled && network_enabled;
    }

    private void askForLocation()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        builder.setMessage(R.string.dialog_msg_gps);

        // Set up the buttons
        builder.setPositiveButton(R.string.btn_ok, (dialog, which) ->
                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_LOCATION));

        builder.setNegativeButton(R.string.cancel, (dialog, which) ->
                navController.navigateUp());

        builder.show();
    }

    /**
     * checks for ACCESS_FINE_LOCATION granted
     * @return returns boolean if permission granted
     */
    private boolean hasLocationPermissions() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * requesting permission ACCESS_FINE_LOCATION
     */
    private void requestLocationPermission() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
    }

    private final BleScanListener bleScanListener = new BleScanListener() {

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