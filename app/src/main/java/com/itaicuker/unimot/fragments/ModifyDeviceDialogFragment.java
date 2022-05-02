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
import com.itaicuker.unimot.databinding.DialogModifyDeviceBinding;
import com.itaicuker.unimot.models.Device;
import com.itaicuker.unimot.models.Remote;
import com.itaicuker.unimot.repositories.Repository;
import com.itaicuker.unimot.viewModels.RemoteListViewModel;

import java.util.ArrayList;
import java.util.HashMap;

public class ModifyDeviceDialogFragment extends DialogFragment
{
    private final String TAG = "ModifyDeviceDialogFragment";
    DialogModifyDeviceBinding binding;

    RemoteListViewModel remoteListViewModel;
    ArrayList<Remote> remoteIds;

    String config;
    Device device;

    ObservableBoolean isRemoteIdEnabled, isDeviceTypeEnabled;

    RadioGroup rgDeviceType;
    AutoCompleteTextView actRemoteId;
    EditText etDeviceName;
    Button btnPositive;

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

    @Override
    public void onStart() {
        super.onStart();


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

        btnPositive = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
        btnPositive.setEnabled(false);

        remoteIds = new ArrayList<>();
        ArrayAdapter<Remote> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_dropdown_item_1line, remoteIds);
        actRemoteId.setAdapter(adapter);

        //getting remoteIds list from db
        remoteListViewModel = new ViewModelProvider(requireActivity()).get(RemoteListViewModel.class);

        remoteListViewModel.getRemoteListMutableLiveData().observe(requireActivity(), snapshot -> {
            remoteIds.clear();
            remoteIds.addAll(snapshot);
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

//            btnPositive.setEnabled(true);
//            isRemoteIdEnabled.set(true);
//            isDeviceTypeEnabled.set(true);
        }
    }

    private DialogInterface.OnClickListener dialogOnClickListener = (dialog, which) -> {
        if (which == Dialog.BUTTON_POSITIVE) {

            int id = binding.rgDeviceType.getCheckedRadioButtonId();
            RadioButton rb = binding.rgDeviceType.findViewById(id);

            //Device fields
            String remoteId = actRemoteId.getText().toString();
            String deviceType = rb.getText().toString();
            boolean isOnline = remoteIds
                    .stream()
                    .filter(remote -> remote.getId().equals(remoteId)).findFirst().get().isOnline();
            String deviceName = etDeviceName.getText().toString();

            HashMap<Object, Object> map = new HashMap<>();
            map.put("deviceType", deviceType.toUpperCase());
            map.put("isOnline", isOnline);
            map.put("name", deviceName);
            map.put("remoteId", remoteId);

            Repository repository = Repository.getInstance();
            if (config.equals("Create"))
                repository.createDevice(map);
            else if (!repository.isDeviceMap(device, map))  //checking if device wasn't edited
                repository.editDevice(map, device.getuId());
        }
    };

    /**
     * setting fields enabled based on previous fields validity.
     */
    private TextWatcher textChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //unused
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //device name is in range (2-32 char)
            boolean isDeviceNameValid =
                    etDeviceName.getText().length() >= 2 &&
                            etDeviceName.getText().length() <= 36;

            //setting dialog state
            isRemoteIdEnabled.set(isDeviceNameValid);

            //remote id is a valid remote id
            String remoteId = actRemoteId.getText().toString();
            boolean isRemoteIdValid = remoteIds
                            .stream()
                            .anyMatch(remote -> remote.getId().equals(remoteId));

            //setting dialog state
            isDeviceTypeEnabled.set(isRemoteIdValid && isDeviceNameValid);
            btnPositive.setEnabled(
                    rgDeviceType.getCheckedRadioButtonId() != -1 && isDeviceTypeEnabled.get()  // -1 means radio group is unchecked
            );
        }

        @Override
        public void afterTextChanged(Editable s) {
            //unused
        }
    };

    /**
     * setting positive dialog button enabled based on fields validity
     */
    private RadioGroup.OnCheckedChangeListener onCheckedListener = (group, checkedId) -> {
        btnPositive.setEnabled(
                checkedId != -1 && isDeviceTypeEnabled.get()  // -1 means radio group is unchecked
        );
    };


}
