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
    public static final String REQUEST_WIFI_CREDENTIALS = "4";

    private NavController navController;
    private DialogWifiCredentialsBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        //getting navController
        navController = NavHostFragment.findNavController(this);

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

    @Override
    public void onClick(DialogInterface dialog, int which) {

        String SSID = binding.etSSID.getEditText().getText().toString();
        String Pass = binding.etSSIDPassword.getEditText().getText().toString();

        //are credentials valid?
        boolean validCredentials =
                SSID.length() >= 2 && SSID.length() <= 32 &&
                Pass.length() >= 8 & Pass.length() <= 63;

        if (validCredentials && which == dialog.BUTTON_POSITIVE //if positive button and validCredentials or negative button
            || which == dialog.BUTTON_NEGATIVE)
        {
            Bundle bundle = new Bundle();
            bundle.putString("SSID", SSID);
            bundle.putString("Pass", Pass);

            requireActivity().getSupportFragmentManager().setFragmentResult(REQUEST_WIFI_CREDENTIALS, bundle);
            dismiss();
        }
    }
}
