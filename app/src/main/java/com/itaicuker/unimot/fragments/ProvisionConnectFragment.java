package com.itaicuker.unimot.fragments;

import static com.itaicuker.unimot.fragments.WifiCredentialsDialogFragment.REQUEST_WIFI_CREDENTIALS;

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
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.databinding.ObservableBoolean;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.espressif.provisioning.DeviceConnectionEvent;
import com.espressif.provisioning.ESPConstants;
import com.espressif.provisioning.ESPProvisionManager;
import com.espressif.provisioning.listeners.BleScanListener;
import com.itaicuker.unimot.R;
import com.itaicuker.unimot.adapters.BleRemoteListAdapter;
import com.itaicuker.unimot.databinding.FragmentProvisionConnectBinding;
import com.itaicuker.unimot.models.Remote;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * The Fragment Provision connect fragment.
 */
public class ProvisionConnectFragment extends Fragment {

    /**
     * The Request enable bt.
     */
    static final int REQUEST_ENABLE_BT = 1;
    /**
     * The Request fine location.
     */
    static final int REQUEST_FINE_LOCATION = 2;
    /**
     * The Request location.
     */
    static final int REQUEST_LOCATION = 3;
    /**
     * The Prefix for Remotes.
     */
    static final String prefix = "UNIMOT-";
    /**
     * The Proof of possession.
     */
    static final String POP = "unimot";
    private static final String TAG = "UNIMOT: " + ProvisionConnectFragment.class.getSimpleName();
    /**
     * Boolean state for fragment.
     */
    private final ObservableBoolean isRemoteConnected, isConnecting, isScanning, isLvRemotesEmpty;
    /**
     * The Nav controller.
     */
    NavController navController;
    /**
     * The Binding.
     */
    FragmentProvisionConnectBinding binding;
    /**
     * The Btn scan.
     */
    Button btnScan;
    /**
     * The listView remotes.
     */
    ListView lvRemotes;
    /**
     * The Ble adapter.
     */
    BluetoothAdapter bleAdapter;
    /**
     * The remote list Adapter.
     */
    BleRemoteListAdapter adapter;
    /**
     * The Ssid.
     */
    String ssid;
    /**
     * The Password.
     */
    String pass;
    /**
     * The Remote list.
     */
    ArrayList<Remote> remoteList;   //ArrayList to store scanned remotes data
    /**
     * The Bluetooth devices.
     */
    HashMap<BluetoothDevice, String> bluetoothDevices;
    private final BleScanListener bleScanListener = new BleScanListener() {

        @Override
        public void scanStartFailed() {
            Toast.makeText(getActivity(), "Please turn on Bluetooth to connect BLE device", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPeripheralFound(@NonNull BluetoothDevice device, @NonNull ScanResult scanResult) {

            Log.d(TAG, "====== onPeripheralFound ===== " + device.getName());
            String serviceUuid = "";

            if (scanResult.getScanRecord().getServiceUuids() != null && scanResult.getScanRecord().getServiceUuids().size() > 0) {
                serviceUuid = scanResult.getScanRecord().getServiceUuids().get(0).toString();
            }

            //if remote is not in list yet add it and refresh list
            if (!bluetoothDevices.containsKey(device)) {
                lvRemotes.setVisibility(View.VISIBLE);
                Remote remote = new Remote(scanResult.getScanRecord().getDeviceName(), device);

                bluetoothDevices.put(device, serviceUuid);
                remoteList.add(remote);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void scanCompleted() {
            Log.d(TAG, "scanCompleted");
            isScanning.set(false);
            isLvRemotesEmpty.set(remoteList.size() < 1);
        }

        @Override
        public void onFailure(@NonNull Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    };
    /**
     * is in Wifi credentials dialog
     */
    private boolean isInDialog = false;
    /**
     * Boolean state for fragment.
     */
    private boolean isDialogSuccess;
    /**
     * Destination Changed Listener.
     */
    private final NavController.OnDestinationChangedListener destinationChangedListener = (navController, navDestination, bundle) -> {

        if (navDestination.getId() == R.id.provisionConnectFragment) {
            isInDialog = false;
            //returned from WifiCredentialsDialogFragment with success
            if (isDialogSuccess) {
                isDialogSuccess = false;    //eliminate infinite loop
                Bundle credentials = new Bundle();
                credentials.putString("ssid", ssid);
                credentials.putString("pass", pass);
                navController.navigate(R.id.action_provisionConnectFragment_to_provisionStatusFragment, credentials);
            }
        }
    };
    /**
     * The Provision manager singleton.
     */
    private ESPProvisionManager provisionManager;
    /**
     * Wifi Credentials Result Listener.
     */
    private final FragmentResultListener wifiCredentialsResultListener = (requestKey, result) -> {

        ssid = result.getString("ssid", "");
        pass = result.getString("pass", "");

        //dialog success?
        if (!TextUtils.isEmpty(ssid + pass)) {
            isDialogSuccess = true;
        } else
            provisionManager.getEspDevice().disconnectDevice();
    };
    /**
     * Button Scan Click Listener.
     */
    private final View.OnClickListener btnScanClickListener = v -> {
        //start scan
        startScan();
    };

    /**
     * initializing views for layout
     */
    private void initViews() {
        btnScan = binding.btnScan;
        lvRemotes = binding.lvRemotes;

        btnScan.setOnClickListener(btnScanClickListener);

        adapter = new BleRemoteListAdapter(requireContext(), R.layout.item_ble_scan, remoteList);

        lvRemotes.setAdapter(adapter);
        lvRemotes.setOnItemClickListener(onRemoteCLickListener);
    }

    /**
     * Instantiates a new Provision connect fragment.
     */
    public ProvisionConnectFragment() {
        //setting states
        isRemoteConnected = new ObservableBoolean(false);
        isConnecting = new ObservableBoolean(false);
        isScanning = new ObservableBoolean(false);
        isLvRemotesEmpty = new ObservableBoolean(true);
        isDialogSuccess = false;
    }

    @Override
    public void onPause() {
        navController.removeOnDestinationChangedListener(destinationChangedListener);
        super.onPause();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //registering event handler for provisionManager usage
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProvisionConnectBinding.inflate(inflater, container, false);
        setHasOptionsMenu(false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //getting navController
        navController = NavHostFragment.findNavController(this);

        // Checks if Bluetooth LE is supported on this device.
        if (!requireActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(getActivity(), R.string.error_ble_not_supported, Toast.LENGTH_LONG).show();
            navController.navigateUp();
        }
        //getting Bluetooth manager adapter
        final BluetoothManager bluetoothManager = (BluetoothManager) requireContext().getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        Log.d(TAG, bluetoothManager.toString());
        bleAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on this device.
        if (bleAdapter == null) {
            Toast.makeText(getActivity(), R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            navController.navigateUp();
        }

        //binding state booleans to layout
        binding.setIsConnecting(isConnecting);
        binding.setIsScanning(isScanning);
        binding.setIsRemoteConnected(isRemoteConnected);
        binding.setIsLvRemotesEmpty(isLvRemotesEmpty);

        //initializing sets
        bluetoothDevices = new HashMap<>();
        remoteList = new ArrayList<>();

        //getting provisionManager and creating ESPDevice object
        provisionManager = ESPProvisionManager.getInstance(requireContext().getApplicationContext());
        if (provisionManager.getEspDevice() == null)
            //using BLE for communication, SECURITY_1 means its encrypted
            provisionManager.createESPDevice(ESPConstants.TransportType.TRANSPORT_BLE, ESPConstants.SecurityType.SECURITY_1);
        provisionManager.getEspDevice().setProofOfPossession(POP);

        initViews();
    }

    @Override
    public void onResume() {
        //on resume start scan
        navController.addOnDestinationChangedListener(destinationChangedListener);
        if (!isRemoteConnected.get() && !isConnecting.get())    //starting scan if not started yet
            startScan();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (isScanning.get()) { //stopping scan if scanning
            stopScan();
        }
        provisionManager.getEspDevice().disconnectDevice();
        EventBus.getDefault().unregister(this); //unregistering event handler onDestroy fragment
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult, requestCode : " + requestCode + ", resultCode : " + resultCode);


        if (requestCode == REQUEST_ENABLE_BT) {
            // User chose not to enable Bluetooth.
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getActivity(), "Provisioning requires bluetooth", Toast.LENGTH_LONG).show();
                navController.navigateUp();
            }
            //User enabled Bluetooth
            if (resultCode == Activity.RESULT_OK)
                startScan();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Location permission
        if (requestCode == REQUEST_FINE_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                //permission granted
                startScan();
            else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                //permission denied
                Toast.makeText(getActivity(), R.string.toast_location_perm_denied, Toast.LENGTH_LONG).show();
                navController.navigateUp();
            }
        }
    }

    /**
     * On provision event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(@NonNull DeviceConnectionEvent event) {

        Log.d(TAG, "ON Device Prov Event RECEIVED : " + event.getEventType());
        switch (event.getEventType()) {

            case ESPConstants.EVENT_DEVICE_CONNECTED:

                Log.d(TAG, "Device Connected Event Received");
                isConnecting.set(false);
                isRemoteConnected.set(true);

                //navigating to wifi credentials dialog
                navController.navigate(R.id.action_provisionConnectFragment_to_provisionWifiDialogFragment);
                isInDialog = true;

                //listener to wait for result
                requireActivity().getSupportFragmentManager().setFragmentResultListener(REQUEST_WIFI_CREDENTIALS, getViewLifecycleOwner(), wifiCredentialsResultListener);

                break;

            case ESPConstants.EVENT_DEVICE_DISCONNECTED:

                isConnecting.set(false);
                isRemoteConnected.set(false);
                if (isInDialog)
                    navController.navigateUp();
                navController.navigate(R.id.action_global_remoteDisconnectedDialogFragment);
                break;

            case ESPConstants.EVENT_DEVICE_CONNECTION_FAILED:

                isConnecting.set(false);
                isRemoteConnected.set(false);
                alertForRemoteNotSupported();
                break;
        }
    }

    /**
     * Start Remote scan.
     */
    @SuppressLint("MissingPermission")
    private void startScan() {

        if (!hasDependencies() || isScanning.get()) { //checking for dependencies and if already scanning
            return;
        }
        isScanning.set(true);
        remoteList.clear();
        bluetoothDevices.clear();


        provisionManager.searchBleEspDevices(prefix, bleScanListener);
    }

    /**
     * Stop Remote scan.
     */
    @SuppressLint("MissingPermission")
    private void stopScan() {

        isScanning.set(false);
        isLvRemotesEmpty.set(remoteList.size() <= 0);

        if (hasLocationPermissions()) {
            provisionManager.stopBleScan();
        } else {
            Log.e(TAG, "Not able to stop scan as Location permission is not granted.");
            Toast.makeText(getActivity(), "Please give location permission to stop BLE scan", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Alert Dialog for failed to connect to Remote.
     */
    private void alertForRemoteNotSupported() {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setCancelable(false)
                .setTitle(R.string.error_title)
                .setMessage("Failed to connect to remote")
                .setPositiveButton(R.string.btn_ok, (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * checks and requests required permissions of fragment
     * @return true fragment already has permissions, false if requesting permissions.
     */
    private boolean hasDependencies() {

        boolean flag = true;
        //bluetooth not enabled
        if (bleAdapter == null || !bleAdapter.isEnabled()) {
            requestBluetoothEnable();
            flag = false;
        }
        //location not permitted
        if (!hasLocationPermissions()) {
            requestLocationPermission();
            flag = false;
        }
        //location not enabled
        if (!isLocationEnabled()) {
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

    /**
     * Checking location enabled
     *
     * @return is location enabled
     */
    private boolean isLocationEnabled() {

        boolean gps_enabled;
        boolean network_enabled;
        LocationManager lm = (LocationManager) requireActivity().getApplicationContext().getSystemService(Activity.LOCATION_SERVICE);

        gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Log.d(TAG, "GPS Enabled : " + gps_enabled + " , Network Enabled : " + network_enabled);

        return gps_enabled && network_enabled;
    }

    /**
     * Asking for user to enable location services.
     */
    private void askForLocation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
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


    private final AdapterView.OnItemClickListener onRemoteCLickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            //stopping scan
            stopScan();
            //setting state booleans
            isConnecting.set(true);
            isRemoteConnected.set(false);

            Remote remote = adapter.getItem(position);
            String uuid = bluetoothDevices.get(remote.getBluetoothDevice());
            Log.d(TAG, "=================== Connect to remote : " + remote.getId() + " UUID : " + uuid);

            if (hasLocationPermissions()) {
                provisionManager.getEspDevice().connectBLEDevice(remote.getBluetoothDevice(), uuid);
            } else {
                Log.e(TAG, "Not able to connect remote as Location permission is not granted.");
                Toast.makeText(getActivity(), R.string.permmision_location_request, Toast.LENGTH_LONG).show();
            }
        }
    };
}