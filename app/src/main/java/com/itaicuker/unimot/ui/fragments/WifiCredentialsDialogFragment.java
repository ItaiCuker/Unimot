package com.itaicuker.unimot.ui.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;

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

    private EditText etSsid, etPass;
    private Button btnOk;

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

        //init views
        etSsid = binding.etSsid.getEditText();
        etPass = binding.etPass.getEditText();
        etSsid.addTextChangedListener(textWatcher);
        etPass.addTextChangedListener(textWatcher);

        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        btnOk = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

        String ssid = etSsid.getText().toString();
        String pass = etPass.getText().toString();

        Bundle bundle = new Bundle();
        bundle.putString("ssid", ssid);
        bundle.putString("pass", pass);

        requireActivity().getSupportFragmentManager().setFragmentResult(REQUEST_WIFI_CREDENTIALS, bundle);
        dismiss();
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //setting btnOk enabled if inputs are valid
            btnOk.setEnabled(
                    isValidCredentials(etSsid.getText().toString(), etPass.getText().toString()));
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
}
