package com.itaicuker.unimot.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.itaicuker.unimot.R;
import com.itaicuker.unimot.databinding.FragmentDeviceBinding;
import com.itaicuker.unimot.models.Device;
import com.itaicuker.unimot.viewModels.DeviceViewModel;

public class DeviceFragment extends Fragment
{
    private static final String TAG = "DeviceFragment";
    NavController navController;
    FragmentDeviceBinding binding;

    LiveData<Device> deviceLiveData;
    DeviceViewModel deviceViewModel;

    Device device;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDeviceBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        String uId = getArguments().getString("uId");

        deviceViewModel = new ViewModelProvider(this).get(DeviceViewModel.class);
        deviceLiveData = deviceViewModel.getDeviceMutableLiveData(uId);
        deviceLiveData.observe(this, device -> {
            this.device = device;
            if (device == null) {   //device probably no longer exists
                navController.navigateUp();
                Toast.makeText(requireActivity(), "device no longer exists", Toast.LENGTH_LONG).show();
            }
            else {
                Log.d(TAG, device.toString());
                ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                actionBar.setLogo(device.getDeviceType().getIcon());
                actionBar.setTitle(device.getName());
            }

        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.device_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.deviceEdit) { //user pressed edit button
            //navigating to modifyDevice dialog with edit config
            Bundle args = new Bundle();
            args.putString("config", "Edit");
            args.putSerializable("device", device);
            navController.navigate(R.id.action_deviceFragment_to_ModifyDeviceDialogFragment, args);
        }
        else if (item.getItemId() == R.id.deviceDelete) { //user pressed delete button
            //creating alert dialog to warn user
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
            builder
                    .setTitle("Are you sure you want to delete device?")
                    .setPositiveButton("confirm", (dialog, which) -> {
                        deviceViewModel.deleteDevice();
                    })
                    .setNegativeButton("cancel", ((dialog, which) -> dialog.dismiss()))
                    .create()
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }
}