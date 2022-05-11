package com.itaicuker.unimot.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.itaicuker.unimot.R;
import com.itaicuker.unimot.databinding.FragmentDeviceBinding;
import com.itaicuker.unimot.models.Device;
import com.itaicuker.unimot.viewModels.DeviceViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Fragment Device fragment.
 */
public class DeviceFragment extends Fragment {
    private static final String TAG = "UNIMOT: " + DeviceFragment.class.getSimpleName();

    private final float DISABLED = 0.25f;   //alpha button disabled
    private final float ENABLED = 1f;       //alpha button enabled

    /**
     * The Nav controller.
     */
    NavController navController;
    /**
     * The Binding.
     */
    FragmentDeviceBinding binding;

    /**
     * The Device live data.
     */
    LiveData<Device> deviceLiveData;
    /**
     * The Device view model.
     */
    DeviceViewModel deviceViewModel;

    /**
     * The Buttons list.
     */
    List<Button> buttons;

    /**
     * The Device.
     */
    Device device;
    /**
     * The Command map.
     */
    Map<String, String> commandMap;
    private final View.OnClickListener clickListener = v -> {
        //getting commandId
        String commandId = commandMap.get(v.getTag().toString());
        //does command exist and device is available?
        if (device.isAvailable() && commandId != null) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("message", "send");
                obj.put("commandId", commandId);
                obj.put("remoteId", device.getRemoteId());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            deviceViewModel.sendCommand(obj);
        } else
            Toast.makeText(requireActivity(), "Remote is offline or occupied", Toast.LENGTH_SHORT).show();
    };
    private final View.OnLongClickListener longClickListener = v -> {
        String commandId = "";
        //does command exist? if not commandId will be empty string
        if (commandMap.containsKey(v.getTag().toString())) {
            commandId = commandMap.get(v.getTag().toString());
        }
        if (device.isAvailable()) { //go to learn command dialog
            Bundle args = new Bundle();
            args.putString("config", commandId.equals("") ? "Create" : "Edit");
            args.putString("commandName", v.getTag().toString());
            args.putString("commandId", commandId);

            navController.navigate(R.id.action_deviceFragment_to_learnCommandDialogFragment, args);
        } else
            Toast.makeText(requireActivity(), "Remote is offline or occupied", Toast.LENGTH_SHORT).show();
        return false;
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //inflating view
        binding = FragmentDeviceBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);

        //creating buttons list and setting click listeners
        buttons = new ArrayList<>();
        for (int i = 1; i < 16; i++) {
            int id = getResources().getIdentifier("cmd" + i, "id", requireActivity().getPackageName());
            Button btn = binding.getRoot().findViewById(id);
            btn.setOnClickListener(clickListener);
            btn.setOnLongClickListener(longClickListener);
            buttons.add(btn);
        }

        //getting id of device
        String id = getArguments().getString("id");

        deviceViewModel = new ViewModelProvider(navController.getCurrentBackStackEntry()).get(DeviceViewModel.class);
        deviceLiveData = deviceViewModel.getDeviceMutableLiveData(id);
        deviceLiveData.observe(this, device -> {
            this.device = device;
            if (device == null) {   //device probably no longer exists
                navController.navigate(R.id.action_global_MainFragment);
                Toast.makeText(requireActivity(), "device no longer exists", Toast.LENGTH_LONG).show();
            } else {  //update ui other wise
                Log.d(TAG, device.toString());
                updateUI();
            }
        });
    }

    private void updateUI() {

        //updating action bar
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle(device.getName());
        actionBar.setLogo(device.getDeviceType().getIcon());

        //updating buttons state
        commandMap = device.getCommands();

        buttons.forEach((btn) -> {
            //if command exists than alpha is 1f, else 0.25f
            boolean enabled = commandMap.containsKey(btn.getTag().toString());
            btn.setAlpha(enabled ? ENABLED : DISABLED);
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        //inflating custom menu
        inflater.inflate(R.menu.device_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.deviceEdit) { //user pressed edit button
            //navigating to modify Device dialog
            Bundle args = new Bundle();
            args.putString("config", "Edit");
            args.putSerializable("device", device);
            navController.navigate(R.id.action_deviceFragment_to_ModifyDeviceDialogFragment, args);
        } else if (item.getItemId() == R.id.deviceDelete) { //user pressed delete button
            //creating alert dialog to warn user of deletion
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
            builder
                    .setTitle("Are you sure you want to delete device?")
                    .setPositiveButton("confirm", (dialog, which) -> {
                        deviceViewModel.delete();
                    })
                    .setNegativeButton("cancel", ((dialog, which) -> dialog.dismiss()))
                    .create()
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }
}