package com.itaicuker.unimot.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.ObservableBoolean;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.itaicuker.unimot.R;
import com.itaicuker.unimot.databinding.DialogLearnCommandBinding;
import com.itaicuker.unimot.models.Device;
import com.itaicuker.unimot.models.Remote;
import com.itaicuker.unimot.viewModels.DeviceViewModel;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The DialogFragment Learn command dialog fragment.
 */
public class LearnCommandDialogFragment extends DialogFragment {

    private static final String TAG = "UNIMOT: " + LearnCommandDialogFragment.class.getSimpleName();

    /**
     * The Binding.
     */
    DialogLearnCommandBinding binding;
    /**
     * The Nav controller.
     */
    NavController navController;

    /**
     * The Dialog.
     */
    @Nullable
    AlertDialog dialog;

    /**
     * The Is remote received command state boolean.
     */
    ObservableBoolean isRemoteReceivedCommand;
    /**
     * The Is remote tested command state boolean.
     */
    ObservableBoolean isRemoteTestedCommand;


    /**
     * The Is learning state boolean.
     */
    boolean isLearning = false;
    /**
     * remote event listener for learning commands
     */
    private final Remote.RemoteLearnListener remoteLearnListener = (event -> {
        switch (event) {
            case LEARN_START: //learn started
                isLearning = true;
                Toast.makeText(requireActivity(), "Remote started learn", Toast.LENGTH_SHORT).show();
                break;
            case LEARN_REMOTE_RECEIVED_COMMAND: //remote received command
                isRemoteReceivedCommand.set(true);
                break;
            case LEARN_REMOTE_TESTED_COMMAND:   //remote tested command
                Toast.makeText(requireActivity(), "Tested command", Toast.LENGTH_SHORT).show();
                isRemoteTestedCommand.set(true);
                break;
            case LEARN_END: //learn ended
                Toast.makeText(requireActivity(), "Remote fail or end!", Toast.LENGTH_LONG).show();
                dismiss();
                break;
        }
    });
    /**
     * The Config.
     */
    String config;
    /**
     * The Command id.
     */
    String commandId;
    /**
     * The Command name.
     */
    String commandName;
    /**
     * The Remote.
     */
    Remote remote;
    /**
     * The Device view model.
     */
    DeviceViewModel deviceViewModel;
    private final DialogInterface.OnClickListener dialogOnClickListener = ((dialog, which) -> {
        if (which == Dialog.BUTTON_POSITIVE) { //create command
            try {
                JSONObject command = new JSONObject();
                remote.sendCommand(command.put("message", "create"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (which == Dialog.BUTTON_NEGATIVE) // delete command
            deviceViewModel.deleteCommand(commandId, commandName);
        dismiss();
    });
    /**
     * The Device live data.
     */
    LiveData<Device> deviceLiveData;
    /**
     * The Device.
     */
    @Nullable
    Device device;
    /**
     * The Btn create.
     */
    Button btnCreate;
    private final View.OnClickListener onClickListener = v -> {
        JSONObject command = new JSONObject();
        try {
            switch (v.getId()) {
                case R.id.btnTestCmd:
                    remote.sendCommand(command.put("message", "test"));
                    break;
                case R.id.btnYes:
                    btnCreate.setEnabled(true);
                    break;
                case R.id.btnNo:
                    isRemoteReceivedCommand.set(false);
                    isRemoteTestedCommand.set(false);
                    btnCreate.setEnabled(false);
                    remote.sendCommand(command.put("message", "no"));
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    };

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogLearnCommandBinding.inflate(LayoutInflater.from(getContext()));

        //getting arguments supplied when the fragment was instantiated
        commandName = getArguments().getString("commandName");
        commandId = getArguments().getString("commandId");
        config = getArguments().getString("config");
        //build dialog
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
        builder.setMessage(config + " Unimot command")
                .setPositiveButton(config, dialogOnClickListener)
                .setNeutralButton("Cancel", dialogOnClickListener)
                .setView(binding.getRoot());

        if (config == "Edit") { //add delete button only if command exists
            builder.setNegativeButton("Delete", dialogOnClickListener);
        }

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        navController = NavHostFragment.findNavController(this);

        //getting device live data from view model
        deviceViewModel = new ViewModelProvider(navController.getBackStackEntry(R.id.deviceFragment)).get(DeviceViewModel.class);
        deviceLiveData = deviceViewModel.getDeviceMutableLiveData();
        device = deviceLiveData.getValue();

        remote = new Remote(device.getRemoteId(), device.isAvailable());

        deviceLiveData.observe(this, device -> {  //observing device for changes
            this.device = device;
            if (device == null) {
                dismiss();
            }
        });

        //setting on click
        binding.btnTestCmd.setOnClickListener(onClickListener);
        binding.btnNo.setOnClickListener(onClickListener);
        binding.btnYes.setOnClickListener(onClickListener);

        //setting button create state
        dialog = (AlertDialog) getDialog();
        btnCreate = dialog.getButton(Dialog.BUTTON_POSITIVE);
        btnCreate.setEnabled(false);

        //init state booleans
        isRemoteReceivedCommand = new ObservableBoolean(false);
        isRemoteTestedCommand = new ObservableBoolean(false);
        binding.setIsRemoteReceivedCommand(isRemoteReceivedCommand);
        binding.setIsRemoteTestedCommand(isRemoteTestedCommand);

        //starting learn
        remote.startLearnCommand(remoteLearnListener, device.getId(), commandName, commandId);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        //on dismiss stop listen and/or learn
        if (isLearning)
            remote.stopLearn();
        remote.stopListen();
        super.onDismiss(dialog);
    }
}
