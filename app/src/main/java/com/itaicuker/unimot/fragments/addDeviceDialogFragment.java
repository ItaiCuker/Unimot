package com.itaicuker.unimot.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.itaicuker.unimot.databinding.DialogAddDeviceBinding;
import com.itaicuker.unimot.models.Remote;
import com.itaicuker.unimot.viewModels.RemoteListViewModel;

import java.util.ArrayList;

public class addDeviceDialogFragment extends DialogFragment
{
    DialogAddDeviceBinding binding;

    RemoteListViewModel remoteListViewModel;
    ArrayList<String> remoteIds;

    RadioGroup rgDeviceType;
    AutoCompleteTextView actRemoteId;
    EditText edDeviceNae;
    Button btnPositive;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogAddDeviceBinding.inflate(LayoutInflater.from(getContext()));

        //build dialog
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
        builder.setMessage("Create Unimot device")
                .setPositiveButton("create device", dialogOnClickListener)
                .setNegativeButton("Cancel", dialogOnClickListener)
                .setView(binding.getRoot());

        return builder.create();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rgDeviceType = binding.rgDeviceType;
        actRemoteId = binding.actRemoteId;
        edDeviceNae = binding.edDeviceName;

        btnPositive = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);

        remoteIds = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, remoteIds);
        actRemoteId.setAdapter(adapter);

        remoteListViewModel = new ViewModelProvider(this).get(RemoteListViewModel.class);
        remoteListViewModel.getRemoteListMutableLiveData().observe(this, remotes -> {
            remoteIds = new ArrayList<>();
            for (Remote remote : remotes)
                remoteIds.add(remote.getId());
            adapter.notifyDataSetChanged();
        });


        rgDeviceType.setOnCheckedChangeListener(rgDeviceTypeOnCheckedListener);
        actRemoteId.setOnFocusChangeListener(actOnFocusChangeListener);
    }


    private DialogInterface.OnClickListener dialogOnClickListener = (dialog, which) -> {

    };

    private RadioGroup.OnCheckedChangeListener rgDeviceTypeOnCheckedListener = (group, checkedId) -> {

    };

    private View.OnFocusChangeListener actOnFocusChangeListener = (v, hasFocus) -> {
        if (!hasFocus) { //no focus and text is exactly or longer than ID
            if (actRemoteId.getText().toString().length() >= 13) {
                actRemoteId.setError("Invalid Remote ID");
                btnPositive.setEnabled(false);
            }
            else
                btnPositive.setEnabled(true);
        }
        if (hasFocus)
            actRemoteId.showDropDown();
    };

}
