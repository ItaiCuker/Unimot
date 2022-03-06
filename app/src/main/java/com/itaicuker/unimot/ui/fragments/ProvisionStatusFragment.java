package com.itaicuker.unimot.ui.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.espressif.provisioning.DeviceConnectionEvent;
import com.espressif.provisioning.ESPConstants;
import com.espressif.provisioning.ESPProvisionManager;
import com.espressif.provisioning.listeners.ProvisionListener;
import com.itaicuker.unimot.R;
import com.itaicuker.unimot.databinding.FragmentProvisionStatusBinding;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ProvisionStatusFragment extends Fragment {

    private static final String TAG = "ProvisionStatusFragment";    //for logs

    private FragmentProvisionStatusBinding binding;
    private NavController navController;
    private ESPProvisionManager provisionManager;

    //views
    private TextView tvProvError;
    private ImageView tickSending, tickApplying, tickChecking;
    private ContentLoadingProgressBar progSending, progApplying, progChecking;

    //credentials
    private String ssid, pass;

    //state of provisioning
    private boolean isProvisioningCompleted = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //using databinding to inflate
        binding = FragmentProvisionStatusBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //getting navController
        navController = Navigation.findNavController(view);

        //getting arguments from parent fragment
        Bundle bundle = getArguments();
        ssid = bundle.getString("ssid");
        pass = bundle.getString("pass");


        provisionManager = ESPProvisionManager.getInstance(requireContext().getApplicationContext());

        EventBus.getDefault().register(this);   //registering event handler for provisionManager usage

        doProvisioning();
    }

    private void initViews()
    {
        tvProvError = binding.tvProvError;

        tickSending = binding.tickSending;
        tickApplying = binding.tickApplying;
        tickChecking = binding.tickChecking;

        progSending = binding.progSending;
        progApplying = binding.progApplying;
        progChecking = binding.progChecking;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.provision_menu, menu);  //changing options menu
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home)  //handling when user pressed back button
        {
            provisionManager.getEspDevice().disconnectDevice();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this); //unregistering onDestroy
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DeviceConnectionEvent event) {

        Log.d(TAG, "On Device Connection Event RECEIVED : " + event.getEventType());

        switch (event.getEventType()) {

            case ESPConstants.EVENT_DEVICE_DISCONNECTED:
                if (!isRemoving() && !isProvisioningCompleted) {
                    showAlertForDeviceDisconnected();
                }
                break;
        }
    }


    private void doProvisioning() {

        tickSending.setVisibility(GONE);
        progSending.setVisibility(VISIBLE);

        provisionManager.getEspDevice().provision(ssid, pass, new ProvisionListener() {
            @Override
            public void createSessionFailed(Exception e) {
                //setting ui to session failed state
                tickSending.setImageResource(R.drawable.ic_error);
                tickSending.setVisibility(VISIBLE);
                progSending.setVisibility(GONE);
                tvProvError.setText(R.string.error_session_creation);
            }

            @Override
            public void wifiConfigSent() {
                //setting ui to wifi config sent state
                tickSending.setImageResource(R.drawable.ic_checkbox_on);
                tickSending.setVisibility(VISIBLE);
                progSending.setVisibility(GONE);

                //setting waiting to config applied state
                tickApplying.setVisibility(GONE);
                progApplying.setVisibility(VISIBLE);
            }

            @Override
            public void wifiConfigFailed(Exception e) {
                //setting ui to wifi config failed state
                tickSending.setImageResource(R.drawable.ic_error);
                tickSending.setVisibility(View.VISIBLE);
                progSending.setVisibility(View.GONE);
                tvProvError.setText(R.string.error_prov_step_sending);
            }

            @Override
            public void wifiConfigApplied() {
                //setting ui to wifi config applied state
                tickApplying.setImageResource(R.drawable.ic_checkbox_on);
                tickApplying.setVisibility(View.VISIBLE);
                progApplying.setVisibility(View.GONE);

                //setting waiting to config checked state
                tickChecking.setVisibility(View.GONE);
                progChecking.setVisibility(View.VISIBLE);
            }

            @Override
            public void wifiConfigApplyFailed(Exception e) {
                //setting ui to wifi apply failed state
                tickApplying.setImageResource(R.drawable.ic_error);
                tickApplying.setVisibility(View.VISIBLE);
                progApplying.setVisibility(View.GONE);
                tvProvError.setText(R.string.error_prov_step_applying);
            }

            @Override
            public void provisioningFailedFromDevice(ESPConstants.ProvisionFailureReason failureReason) {
                switch (failureReason) {    //setting error message
                    case AUTH_FAILED:
                         tvProvError.setText(R.string.error_authentication_failed);
                        break;
                    case NETWORK_NOT_FOUND:
                         tvProvError.setText(R.string.error_network_not_found);
                        break;
                    case DEVICE_DISCONNECTED:
                    case UNKNOWN:
                         tvProvError.setText(R.string.error_prov_step_checking);
                        break;
                }
                //setting ui to wifi provisioning failed from device failed state
                tickChecking.setImageResource(R.drawable.ic_error);
                tickChecking.setVisibility(View.VISIBLE);
                progChecking.setVisibility(View.GONE);
            }

            @Override
            public void deviceProvisioningSuccess() {
                //setting ui to success state
                isProvisioningCompleted = true;
                tickChecking.setImageResource(R.drawable.ic_checkbox_on);
                tickChecking.setVisibility(View.VISIBLE);
                progChecking.setVisibility(View.GONE);
            }

            @Override
            public void onProvisioningFailed(Exception e) {
                //setting ui to provisioning failed failed state
                tickChecking.setImageResource(R.drawable.ic_error);
                tickChecking.setVisibility(View.VISIBLE);
                progChecking.setVisibility(View.GONE);
                tvProvError.setText(R.string.error_prov_step_checking);
            }
        });
    }

    private void showAlertForDeviceDisconnected() {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setCancelable(false);
        builder.setTitle(R.string.error_title);
        builder.setMessage(R.string.dialog_msg_ble_device_disconnection);

        // Set up the buttons
        builder.setPositiveButton(R.string.btn_ok, (dialog, which) -> {
            dialog.dismiss();
            navController.navigateUp();
        });

        builder.show();
    }
}
