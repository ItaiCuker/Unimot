package com.itaicuker.unimot.ui.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.itaicuker.unimot.R;
import com.itaicuker.unimot.databinding.DialogWifiCredentialsBinding;

public class WifiCredentialsDialogFragment extends DialogFragment implements DialogInterface.OnClickListener
{
    private static final String TAG = "WifiCredentialsDialogFragment";

    private NavController navController;
    private DialogWifiCredentialsBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        //getting navController
        navController = NavHostFragment.findNavController(this);

        binding = DialogWifiCredentialsBinding.inflate(LayoutInflater.from(getContext()));

        //build dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_provision_wifi)
                .setPositiveButton(R.string.provision_new_remote, this)
                .setNegativeButton(R.string.cancel, this)
                .setView(binding.getRoot());
        setCancelable(false);

        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

        String SSID = binding.etSSID.getEditText().getText().toString();
        String Password = binding.etSSIDPassword.getEditText().getText().toString();

        //if SSID field larger than 2 characters and Password field larger than 8 characters
        if (which == dialog.BUTTON_POSITIVE
            && SSID.length() >= 2
            && Password.length() >= 8)
        {
            navController.getPreviousBackStackEntry().getSavedStateHandle()
                    .set("SSID", SSID);
            navController.getPreviousBackStackEntry().getSavedStateHandle()
                    .set("PASS", Password);

            navController.navigateUp(); //dialog complete
        }
        if (which == dialog.BUTTON_NEGATIVE)
        {
            navController.navigateUp(); //dismissing dialog
        }
    }
}
