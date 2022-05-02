package com.itaicuker.unimot;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import androidx.navigation.NavController;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class NetworkChangeReciever extends BroadcastReceiver {

    private static final String TAG = "NetworkChangeReciever";

    private final NavController navController;
    private final Activity parent;

    private Dialog dialog;
    private boolean wasConnected;

    public NetworkChangeReciever(Activity parent, NavController navController) {
        super();
        this.parent = parent;
        this.navController = navController;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

         boolean isConnected = ((NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)).isConnected();
         if (!isConnected) {
             navController.navigate(R.id.action_global_MainFragment);
             MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(parent);
             builder.setTitle("Alert!")
                     .setMessage("Phone is offline, please reconnect")
                     .setCancelable(false);
             dialog = builder.show();
         }
         if (!wasConnected && isConnected && dialog != null)
             dialog.dismiss();
         wasConnected = isConnected;

    }
}
