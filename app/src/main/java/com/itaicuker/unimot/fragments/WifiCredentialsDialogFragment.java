package com.itaicuker.unimot.fragments;

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
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.espressif.provisioning.ESPProvisionManager;
import com.espressif.provisioning.WiFiAccessPoint;
import com.espressif.provisioning.listeners.WiFiScanListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.itaicuker.unimot.R;
import com.itaicuker.unimot.databinding.DialogWifiCredentialsBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * The DialogFragment Wifi credentials dialog fragment.
 */
public class WifiCredentialsDialogFragment extends DialogFragment {
    /**
     * The constant REQUEST_WIFI_CREDENTIALS.
     */
    public static final String REQUEST_WIFI_CREDENTIALS = "4";
    private static final String TAG = "UNIMOT: " + WifiCredentialsDialogFragment.class.getSimpleName();
    /**
     * The Binding.
     */
    DialogWifiCredentialsBinding binding;
    /**
     * The Provision manager.
     */
    ESPProvisionManager provisionManager;

    /**
     * The Dialog.
     */
    @Nullable
    AlertDialog dialog;

    /**
     * The EditText pass.
     */
    EditText etPass;
    /**
     * The AutoCompleteTextView ssid.
     */
    AutoCompleteTextView actSsid;
    private final DialogInterface.OnClickListener dialogClickListener = ((dialog, which) -> {
        String ssid = actSsid.getText().toString();
        String pass = etPass.getText().toString();

        Bundle bundle = new Bundle();
        //if dialog wasn't canceled
        if (!TextUtils.isEmpty(ssid + pass)) {
            bundle.putString("ssid", ssid);
            bundle.putString("pass", pass);
        }

        dismiss();
        requireActivity().getSupportFragmentManager().setFragmentResult(REQUEST_WIFI_CREDENTIALS, bundle);
    });
    /**
     * The Btn send.
     */
    Button btnSend;
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //setting btnSend enabled if inputs are valid
            btnSend.setEnabled(
                    isValidCredentials(actSsid.getText().toString(), etPass.getText().toString()));
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    /**
     * The Btn cancel.
     */
    Button btnCancel;
    /**
     * The Is scanning.
     */
    ObservableBoolean isScanning;
    /**
     * Wifi Scan Listener
     */
    private final WiFiScanListener wifiScanListener = new WiFiScanListener() {
        @Override
        public void onWifiListReceived(@NonNull ArrayList<WiFiAccessPoint> wifiList) {
            Log.d(TAG, "AccessPoints recieved");

            //using Ui thread
            requireActivity().runOnUiThread(() -> {
                //enabling cancel and changing state
                btnCancel.setEnabled(true);
                btnSend.setEnabled(true);
                isScanning.set(false);

                //getting list of ssid's in AutoCompleteTextView:
                List<String> listSsids = new ArrayList<>();   //converting WiFiAccessPoint list to String list with SSID values
                for (WiFiAccessPoint accessPoint : wifiList)
                    listSsids.add(accessPoint.getWifiName());

                //creating and setting adapter
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, listSsids);
                actSsid.setAdapter(adapter);

                //ensuring drop down visibility:
                actSsid.setOnFocusChangeListener((v, hasFocus) -> {
                    if (v.getId() == R.id.actSsid && hasFocus)
                        actSsid.showDropDown();
                });
                actSsid.setOnClickListener((v) -> {
                    actSsid.showDropDown();
                });
            });
        }

        @Override
        public void onWiFiScanFailed(Exception e) {
            //using Ui thread
            requireActivity().runOnUiThread(() -> {//scan failed, continuing anyways
                //enabling cancel and changing state
                btnCancel.setEnabled(true);
                btnSend.setEnabled(true);
                isScanning.set(false);
            });
        }
    };
    /**
     * The Nav controller.
     */
    NavController navController;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        binding = DialogWifiCredentialsBinding.inflate(LayoutInflater.from(getContext()));

        //build dialog
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
        builder.setMessage(R.string.dialog_provision_wifi)
                .setPositiveButton(R.string.provision_new_remote, dialogClickListener)
                .setNegativeButton(R.string.cancel, dialogClickListener)
                .setView(binding.getRoot());
        setCancelable(false);

        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        dialog = (AlertDialog) getDialog();
        navController = NavHostFragment.findNavController(this);

        //init views
        actSsid = binding.actSsid;
        etPass = binding.etPass;
        btnSend = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btnCancel = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        //add text changed listeners
        actSsid.addTextChangedListener(textWatcher);
        etPass.addTextChangedListener(textWatcher);

        //setting boolean with data binding
        isScanning = new ObservableBoolean(true);
        binding.setIsScanning(isScanning);

        //waiting to receive remotes AP scan
        btnCancel.setEnabled(false);
        btnSend.setEnabled(false);

        //starting wifi scan
        provisionManager = ESPProvisionManager.getInstance(requireActivity().getApplicationContext());
        startWifiScan();
    }

    /**
     * validating ssid and pass.
     *
     * @param ssid user input
     * @param pass user input
     * @return true if meets requirements -
     * ssid - between 2-32 characters.
     * pass - between 8-63 characters.
     */
    private boolean isValidCredentials(@NonNull String ssid, @NonNull String pass) {
        return ssid.length() >= 2 && ssid.length() <= 32 &&
                pass.length() >= 8 & pass.length() <= 63;
    }

    /**
     * Start Wifi Scan
     */
    private void startWifiScan() {
        Log.d(TAG, "Start WiFi scan");

        //registering event handler
        provisionManager.getEspDevice().scanNetworks(wifiScanListener);
    }
}
