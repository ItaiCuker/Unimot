package com.itaicuker.unimot.fragments;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.itaicuker.unimot.R;

/**
 * The DialogFragment Remote disconnected dialog fragment.
 */
public class RemoteDisconnectedDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
        builder
                .setTitle(R.string.error_title)
                .setMessage("Remote disconnected")
                .setPositiveButton(R.string.btn_ok, (dialog, which) -> dismiss());
        return builder.create();
    }
}
