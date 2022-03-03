package com.itaicuker.unimot.ui.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.itaicuker.unimot.R;
import com.itaicuker.unimot.databinding.DialogProvisionWifiBinding;

public class ProvisionWifiDialogFragment extends DialogFragment implements DialogInterface.OnClickListener
{
    NavController navController;
    EditText etSSID, etSSIDPassword;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        //build dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_provision_wifi)
                .setPositiveButton(R.string.provision_new_remote, this)
                .setNegativeButton(R.string.cancel, this)
                .setView(R.layout.dialog_provision_wifi);

        //init views
        etSSID = getActivity().findViewById(R.id.etSSID);
        etSSIDPassword = getActivity().findViewById(R.id.etSSIDPassword);

        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

        //if SSID field larger than 2 characters and Password field larger than 8 characters
        if (which == dialog.BUTTON_POSITIVE
            && etSSID.getText().toString().length() >= 2
            && etSSIDPassword.getText().toString().length() >= 8)
        {
            navController.getPreviousBackStackEntry().getSavedStateHandle()
                    .set("SSID", etSSID.getText().toString());
            navController.getPreviousBackStackEntry().getSavedStateHandle()
                    .set("PASS", etSSIDPassword.getText().toString());

            navController.navigateUp(); //dismissing dialog
        }
        //user canceled dialog
        if (which == dialog.BUTTON_NEGATIVE)
        {
            navController.navigateUp(); //dismissing dialog
        }
    }
}
