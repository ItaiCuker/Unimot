package com.itaicuker.unimot.ui.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.ObservableBoolean;
import androidx.fragment.app.DialogFragment;

import com.espressif.provisioning.DeviceConnectionEvent;
import com.espressif.provisioning.ESPConstants;
import com.espressif.provisioning.ESPProvisionManager;
import com.espressif.provisioning.WiFiAccessPoint;
import com.espressif.provisioning.listeners.WiFiScanListener;
import com.itaicuker.unimot.R;
import com.itaicuker.unimot.databinding.DialogWifiCredentialsBinding;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WifiCredentialsDialogFragment extends DialogFragment implements DialogInterface.OnClickListener
{
    public static final String REQUEST_WIFI_CREDENTIALS = "4";
    private static final String TAG = "WifiCredentialsDialogFragment";

    private DialogWifiCredentialsBinding binding;
    private ESPProvisionManager provisionManager;

    private AlertDialog dialog;

    private ArrayAdapter adapter;

    private EditText etPass;
    private AutoCompleteTextView acetSsid;
    private Button btnOk, btnCancel;
    
    private ObservableBoolean isScanning;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        binding = DialogWifiCredentialsBinding.inflate(LayoutInflater.from(getContext()));

        //build dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage(R.string.dialog_provision_wifi)
                .setPositiveButton(R.string.provision_new_remote, this)
                .setNegativeButton(R.string.cancel, this)
                .setView(binding.getRoot());
        setCancelable(false);

        // Create the AlertDialog object and return it
        return builder.create();
    }

    /**
     * Remove dialog.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        dialog = (AlertDialog) getDialog();

        //init views
        acetSsid = binding.acetSsid;
        etPass = binding.etPass;
        btnOk = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btnCancel = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        acetSsid.addTextChangedListener(textWatcher);
        etPass.addTextChangedListener(textWatcher);

        //waiting to recieve remotes AP scan
        btnCancel.setEnabled(false);
        btnOk.setEnabled(false);

        //setting boolean with data binding
        binding.setLifecycleOwner(this);
        isScanning = new ObservableBoolean(true);
        binding.setIsScanning(isScanning);

        //starting scan and getting provisionManager
        provisionManager = provisionManager.getInstance(requireActivity().getApplicationContext());
        startWifiScan();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

        String ssid = acetSsid.getText().toString();
        String pass = etPass.getText().toString();

        Bundle bundle = new Bundle();
        if (!TextUtils.isEmpty(ssid + pass)){
            bundle.putString("ssid", ssid);
            bundle.putString("pass", pass);
        }

        requireActivity().getSupportFragmentManager().setFragmentResult(REQUEST_WIFI_CREDENTIALS, bundle);
        dismiss();
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //setting btnOk enabled if inputs are valid
            btnOk.setEnabled(
                    isValidCredentials(acetSsid.getText().toString(), etPass.getText().toString()));
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    /**
     * validating ssid and pass.
     *
     * @param ssid user input
     * @param pass user input
     * @return true if meets requirements -
     * ssid - between 2-32 characters.
     * pass - between 8-63 characters.
     */
    private boolean isValidCredentials(String ssid, String pass) {
        return ssid.length() >= 2 && ssid.length() <= 32 &&
                pass.length() >= 8 & pass.length() <= 63;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DeviceConnectionEvent event) {

        Log.d(TAG, "On Device Connection Event RECEIVED : " + event.getEventType());

        switch (event.getEventType()) {

            case ESPConstants.EVENT_DEVICE_DISCONNECTED:
                if (!isRemoving()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                    builder.setCancelable(false);
                    builder.setTitle(R.string.error_title);
                    builder.setMessage(R.string.dialog_msg_ble_device_disconnection);

                    builder.setPositiveButton(R.string.btn_ok, (dialog, which) -> {
                        //TODO: alert parent about this
                    });

                    builder.show();
                }
                break;
        }
    }

    private void startWifiScan() {
        Log.d(TAG, "Start WiFi scan");

        EventBus.getDefault().register(this);   //registering event handler
        provisionManager.getEspDevice().scanNetworks(new WiFiScanListener() {
            @Override
            public void onWifiListReceived(ArrayList<WiFiAccessPoint> wifiList) {

                //getting list of ssid's in AutoCompleteTextView:

                List<String> listSsid = wifiList.stream()   //converting WiFiAccessPoint list to String list with SSID values
                        .map(WiFiAccessPoint::getWifiName)
                        .collect(Collectors.toList());
                //creating and setting adapter
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.item_ap_scan, listSsid);
                acetSsid.setAdapter(adapter);

                //enabling cancel and changing state
                btnCancel.setEnabled(true);
                btnOk.setEnabled(true);
                isScanning.set(false);
            }

            @Override
            public void onWiFiScanFailed(Exception e) {
                //scan failed, continuing anyways
                //enabling cancel and changing state
                btnCancel.setEnabled(true);
                btnOk.setEnabled(true);
                isScanning.set(false);
            }
        });
    }

}
