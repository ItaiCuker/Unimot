package com.itaicuker.unimot.listeners;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.itaicuker.unimot.R;

/**
 * The type Network change receiver.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    private static final String TAG = "UNIMOT: " + NetworkChangeReceiver.class.getSimpleName();

    private final NavController navController;
    private final Activity activity;

    private Dialog dialog;
    private boolean wasConnected;

    /**
     * Instantiates a new Network change receiver.
     *
     * @param activity      the activity
     * @param navController the nav controller
     */
    public NetworkChangeReceiver(Activity activity, NavController navController) {
        super();
        this.activity = activity;
        this.navController = navController;
    }

    @Override
    public void onReceive(Context context, @NonNull Intent intent) {

        //connected to internet?
        boolean isConnected = ((NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)).isConnected();
        if (!isConnected) {
            //navigating to main and starting reconnect dialog
            navController.navigate(R.id.action_global_MainFragment);
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);
            builder.setTitle("Alert!")
                    .setMessage("Phone is offline, please reconnect")
                    .setCancelable(false);
             dialog = builder.show();
         }
        //if connected again dismiss dialog
         if (!wasConnected && isConnected && dialog != null)
             dialog.dismiss();
         wasConnected = isConnected;

    }
}
