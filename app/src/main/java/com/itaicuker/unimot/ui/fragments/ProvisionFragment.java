package com.itaicuker.unimot.ui.fragments;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.espressif.provisioning.ESPProvisionManager;
import com.itaicuker.unimot.R;
import com.itaicuker.unimot.databinding.FragmentProvisionBinding;
import com.itaicuker.unimot.ui.adapters.BleRemoteListAdapter;
import com.itaicuker.unimot.ui.models.Remote;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;


public class ProvisionFragment extends Fragment {

    private FragmentActivity parentActivity;
    
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
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        parentActivity = requireActivity();

        //getting navController
        navController = Navigation.findNavController(view);

        if (!parentActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            /* TODO: maybe change to dialog */
            Toast.makeText(parentActivity, R.string.error_ble_not_supported, Toast.LENGTH_LONG).show();
            /* TODO: navigate up */
        }
        
        final BluetoothManager bluetoothManager = (BluetoothManager) parentActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        bleAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (bleAdapter == null) {
            /* TODO: maybe change to dialog */
            Toast.makeText(parentActivity, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            /* TODO: navigate up */
        }

        isConnecting = false;
        isRemoteConnected = false;
        handler = new Handler();
        bluetoothRemotes = new HashMap<>();
        remoteList = new ArrayList<>();

        provisionManager = ESPProvisionManager.getInstance(requireActivity().getApplicationContext());

        initViews();

        EventBus.getDefault().register(this);

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
        listRemotes.setOnItemClickListener(onRemoteClickListenr);
    }

    private AdapterView.OnItemClickListener onDeviceCLickListener = new AdapterView.OnItemClickListener() {

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
}