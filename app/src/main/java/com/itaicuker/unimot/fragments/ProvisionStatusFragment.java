package com.itaicuker.unimot.fragments;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.databinding.ObservableBoolean;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.espressif.provisioning.DeviceConnectionEvent;
import com.espressif.provisioning.ESPConstants;
import com.espressif.provisioning.ESPProvisionManager;
import com.espressif.provisioning.listeners.ProvisionListener;
import com.itaicuker.unimot.R;
import com.itaicuker.unimot.databinding.FragmentProvisionStatusBinding;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * The Fragment Provision status fragment.
 */
public class ProvisionStatusFragment extends Fragment {

    private static final String TAG = "UNIMOT: " + ProvisionStatusFragment.class.getSimpleName();    //for logs

    /**
     * The Binding.
     */
    FragmentProvisionStatusBinding binding;
    /**
     * The Nav controller.
     */
    NavController navController;
    /**
     * The Provision manager.
     */
    ESPProvisionManager provisionManager;
    /**
     * Btn finish
     */
    Button btnFinish;
    /**
     * The Tv prov error.
     */
    TextView tvProvError;
    /**
     * The Tick sending
     */
    ImageView tickSending;
    /**
     * The Tick applying.
     */
    ImageView tickApplying;
    /**
     * The Tick checking.
     */
    ImageView tickChecking;
    /**
     * The Prog sending.
     */
    ContentLoadingProgressBar progSending;
    /**
     * The Prog applying.
     */
    ContentLoadingProgressBar progApplying;
    /**
     * The Prog checking.
     */
    ContentLoadingProgressBar progChecking;

    /**
     * The Ssid.
     */
    String ssid;
    /**
     * The Pass.
     */
    String pass;

    /**
     * Is Provision complete state boolean
     */
    ObservableBoolean isProvCompleted;
    private final ProvisionListener provisionListener = new ProvisionListener() {
        @Override
        public void createSessionFailed(Exception e) {
            //using Ui thread
            requireActivity().runOnUiThread(() -> {
                //setting ui to session failed state
                tickSending.setImageResource(R.drawable.ic_error);
                tickSending.setVisibility(VISIBLE);
                progSending.setVisibility(INVISIBLE);
                tvProvError.setText(R.string.error_session_creation);
            });

        }

        @Override
        public void wifiConfigSent() {
            //using Ui thread
            requireActivity().runOnUiThread(() -> {
                //setting ui to wifi config sent state
                tickSending.setImageResource(R.drawable.ic_checkbox_on);
                tickSending.setVisibility(VISIBLE);
                progSending.setVisibility(INVISIBLE);

                //setting waiting to config applied state
                tickApplying.setVisibility(INVISIBLE);
                progApplying.setVisibility(VISIBLE);
            });
        }

        @Override
        public void wifiConfigFailed(Exception e) {
            //using Ui thread
            requireActivity().runOnUiThread(() -> {
                //setting ui to wifi config failed state
                tickSending.setImageResource(R.drawable.ic_error);
                tickSending.setVisibility(View.VISIBLE);
                progSending.setVisibility(View.INVISIBLE);
                tvProvError.setText(R.string.error_prov_step_sending);
            });
        }

        @Override
        public void wifiConfigApplied() {
            //using Ui thread
            requireActivity().runOnUiThread(() -> {
                //setting ui to wifi config applied state
                tickApplying.setImageResource(R.drawable.ic_checkbox_on);
                tickApplying.setVisibility(View.VISIBLE);
                progApplying.setVisibility(View.INVISIBLE);

                //setting waiting to config checked state
                tickChecking.setVisibility(View.INVISIBLE);
                progChecking.setVisibility(View.VISIBLE);
                tvProvError.setText("If provisioning takes too long try resetting remote with EN button");
            });
        }

        @Override
        public void wifiConfigApplyFailed(Exception e) {
            //using Ui thread
            requireActivity().runOnUiThread(() -> {
                //setting ui to wifi apply failed state
                tickApplying.setImageResource(R.drawable.ic_error);
                tickApplying.setVisibility(View.VISIBLE);
                progApplying.setVisibility(View.INVISIBLE);
                tvProvError.setText(R.string.error_prov_step_applying);
            });
        }

        @Override
        public void provisioningFailedFromDevice(@NonNull ESPConstants.ProvisionFailureReason failureReason) {
            //using Ui thread
            requireActivity().runOnUiThread(() -> {
                switch (failureReason) {    //setting error message
                    case AUTH_FAILED:
                        tvProvError.setText(R.string.error_authentication_failed);
                        break;
                    case NETWORK_NOT_FOUND:
                        tvProvError.setText(R.string.error_network_not_found);
                        break;
                    case DEVICE_DISCONNECTED:
                        tvProvError.setText("Remote Disconnected, Provision worked if Remote stopped blinking");
                    case UNKNOWN:
                        tvProvError.setText(R.string.error_prov_step_checking);
                        break;
                }
                //setting ui to wifi provisioning failed from device failed state
                tickChecking.setImageResource(R.drawable.ic_error);
                tickChecking.setVisibility(View.VISIBLE);
                progChecking.setVisibility(View.INVISIBLE);
            });
        }

        @Override
        public void deviceProvisioningSuccess() {
            //using Ui thread
            isProvCompleted.set(true);
            requireActivity().runOnUiThread(() -> {
                //setting ui to success state
                tickChecking.setImageResource(R.drawable.ic_checkbox_on);
                tickChecking.setVisibility(View.VISIBLE);
                progChecking.setVisibility(View.INVISIBLE);
            });
        }

        @Override
        public void onProvisioningFailed(Exception e) {
            //using Ui thread
            requireActivity().runOnUiThread(() -> {
                //setting ui to provisioning failed failed state
                tickChecking.setImageResource(R.drawable.ic_error);
                tickChecking.setVisibility(View.VISIBLE);
                progChecking.setVisibility(View.INVISIBLE);
                tvProvError.setText(R.string.error_prov_step_checking);
            });
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProvisionStatusBinding.inflate(inflater, container, false);
        setHasOptionsMenu(false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);

        //getting arguments from parent fragment
        Bundle bundle = getArguments();
        ssid = bundle.getString("ssid");
        pass = bundle.getString("pass");

        //setting state
        isProvCompleted = new ObservableBoolean(false);
        binding.setIsProvCompleted(isProvCompleted);

        //on button finish click navigate back to main
        binding.btnFinish.setOnClickListener(v ->
                navController.navigate(R.id.action_global_MainFragment));

        provisionManager = ESPProvisionManager.getInstance(requireContext().getApplicationContext());

        EventBus.getDefault().register(this);   //registering event handler for provisionManager usage

        initViews();
        doProvisioning();
    }

    /**
     * Initializing views
     */
    private void initViews() {
        btnFinish = binding.btnFinish;
        //on button finish click return to main
        btnFinish.setOnClickListener((v) -> navController.navigate(R.id.action_global_MainFragment));

        tvProvError = binding.tvProvError;

        tickSending = binding.tickSending;
        tickApplying = binding.tickApplying;
        tickChecking = binding.tickChecking;

        progSending = binding.progSending;
        progApplying = binding.progApplying;
        progChecking = binding.progChecking;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home)  //handling when user pressed back button
        {
            if (isProvCompleted.get())
                navController.navigate(R.id.action_global_MainFragment);
            else
                provisionManager.getEspDevice().disconnectDevice();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        //unregistering provision event handler
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    /**
     * On event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(@NonNull DeviceConnectionEvent event) {

        Log.d(TAG, "On Device Connection Event RECEIVED : " + event.getEventType());

        switch (event.getEventType()) {

            case ESPConstants.EVENT_DEVICE_DISCONNECTED:
                if (!isRemoving() && !isProvCompleted.get()) {
                    navController.navigate(R.id.action_global_remoteDisconnectedDialogFragment);
                }
                break;
        }
    }

    /**
     * Do provisioning
     */
    private void doProvisioning() {

        tickSending.setVisibility(INVISIBLE);
        progSending.setVisibility(VISIBLE);

        provisionManager.getEspDevice().provision(ssid, pass, provisionListener);
    }
}
