package com.itaicuker.unimot.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.ObservableBoolean;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.itaicuker.unimot.Repository;
import com.itaicuker.unimot.databinding.DialogModifyDeviceBinding;
import com.itaicuker.unimot.models.Device;
import com.itaicuker.unimot.models.Remote;
import com.itaicuker.unimot.viewModels.RemoteListViewModel;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The DialogFragment Modify device dialog fragment.
 */
public class ModifyDeviceDialogFragment extends DialogFragment {
    private static final String TAG = "UNIMOT: " + ModifyDeviceDialogFragment.class.getSimpleName();
    /**
     * The Binding.
     */
    DialogModifyDeviceBinding binding;
    /**
     * The Remote list view model.
     */
    RemoteListViewModel remoteListViewModel;
    /**
     * The Remote list.
     */
    ArrayList<Remote> remoteList;
    /**
     * The Config.
     */
    String config;
    /**
     * The Device.
     */
    Device device;
    /**
     * The Is remote id enabled.
     */
    ObservableBoolean isRemoteIdEnabled;
    /**
     * The Is device type enabled.
     */
    ObservableBoolean isDeviceTypeEnabled;
    /**
     * The Rg device type.
     */
    RadioGroup rgDeviceType;
    /**
     * The Act remote id.
     */
    AutoCompleteTextView actRemoteId;
    /**
     * The Et device name.
     */
    EditText etDeviceName;
    /**
     * dialog on click listener
     */
    private final DialogInterface.OnClickListener dialogOnClickListener = (dialog, which) -> {
        if (which == Dialog.BUTTON_POSITIVE) {

            //getting checked radio button
            int id = binding.rgDeviceType.getCheckedRadioButtonId();
            RadioButton rb = binding.rgDeviceType.findViewById(id);

            //getting  device fields
            String remoteId = actRemoteId.getText().toString();
            String deviceType = rb.getText().toString();
            boolean isAvailable = remoteList //getting remote isAvailable
                    .stream()
                    .filter(remote -> remote.getId().equals(remoteId)).findFirst().get().isAvailable();
            String deviceName = etDeviceName.getText().toString();

            //creating map of device fields
            HashMap<Object, Object> map = new HashMap<>();
            map.put("deviceType", deviceType.toUpperCase());
            map.put("isAvailable", isAvailable);
            map.put("name", deviceName);
            map.put("remoteId", remoteId);

            //creating or editing device
            Repository repository = Repository.getInstance();
            if (config.equals("Create"))
                repository.createDevice(map);
            else if (!repository.compareDeviceToMap(device, map))  //checking if device wasn't edited
                repository.editDevice(map, device.getId());
        }
    };

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogModifyDeviceBinding.inflate(LayoutInflater.from(getContext()));

        config = getArguments().getString("config");

        //build dialog
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
        builder.setMessage(config + " Unimot device")
                .setPositiveButton(config + " device", dialogOnClickListener)
                .setNegativeButton("Cancel", dialogOnClickListener)
                .setView(binding.getRoot());

        return builder.create();
    }

    /**
     * The Btn positive.
     */
    Button btnPositive;
    /**
     * setting fields enabled based on previous fields validity.
     */
    private final TextWatcher textChangedListener = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //device name is in range (2-32 char)
            boolean isDeviceNameValid =
                    etDeviceName.getText().length() >= 2 &&
                            etDeviceName.getText().length() <= 36;

            isRemoteIdEnabled.set(isDeviceNameValid);
            if (isDeviceNameValid) {  //if device name is valid check remote id too.

                //remoteId is valid
                String remoteId = actRemoteId.getText().toString();
                boolean isRemoteIdValid = remoteList
                        .stream()
                        .anyMatch(remote -> remote.getId().equals(remoteId));

                //setting dialog state
                isDeviceTypeEnabled.set(isRemoteIdValid);
                btnPositive.setEnabled(
                        rgDeviceType.getCheckedRadioButtonId() != -1 && isDeviceTypeEnabled.get()  // -1 means radio group is unchecked
                );
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //unused
        }

        @Override
        public void afterTextChanged(Editable s) {
            //unused
        }
    };
    /**
     * setting positive dialog button enabled based on fields validity
     */
    private final RadioGroup.OnCheckedChangeListener onCheckedListener = (group, checkedId) -> {
        btnPositive.setEnabled(
                checkedId != -1 && isDeviceTypeEnabled.get()  // -1 means radio group is unchecked
        );
    };

    @Override
    public void onStart() {
        super.onStart();

        //setting state booleans
        isRemoteIdEnabled = new ObservableBoolean(false);
        isDeviceTypeEnabled = new ObservableBoolean(false);
        binding.setIsRemoteIdEnabled(isRemoteIdEnabled);
        binding.setIsDeviceTypeEnabled(isDeviceTypeEnabled);

        rgDeviceType = binding.rgDeviceType;
        actRemoteId = binding.actRemoteId;
        etDeviceName = binding.etDeviceName;


        //listeners for dialog state and field validity.
        rgDeviceType.setOnCheckedChangeListener(onCheckedListener);
        etDeviceName.addTextChangedListener(textChangedListener);
        actRemoteId.addTextChangedListener(textChangedListener);

        //ensuring drop down visibility:
        actRemoteId.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                actRemoteId.showDropDown();
            }
        });
        actRemoteId.setOnClickListener((v) -> {
            actRemoteId.showDropDown();
        });

        //setting positive button
        btnPositive = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
        btnPositive.setEnabled(false);

        //setting adapter for remoteList
        remoteList = new ArrayList<>();
        ArrayAdapter<Remote> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_dropdown_item_1line, remoteList);
        actRemoteId.setAdapter(adapter);

        //getting remoteIds list from db
        remoteListViewModel = new ViewModelProvider(this).get(RemoteListViewModel.class);
        remoteListViewModel.getRemoteListMutableLiveData().observe(requireActivity(), snapshot -> {
            remoteList.clear();
            //adding all remotes.
            remoteList.addAll(snapshot);
            adapter.notifyDataSetChanged();
        });

        //setting fields if coming from device modify button
        if (config.equals("Edit")) {
            device = (Device) getArguments().get("device");
            etDeviceName.setText(device.getName());
            actRemoteId.setText(device.getRemoteId());
            //radio button to check
            final int rb = getResources().getIdentifier("rb" + device.getDeviceType().name(), "id", requireActivity().getPackageName());
            rgDeviceType.check(rb);
        }
    }


}
