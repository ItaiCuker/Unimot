package com.itaicuker.unimot.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.itaicuker.unimot.R;
import com.itaicuker.unimot.databinding.FragmentRemoteBinding;
import com.itaicuker.unimot.models.Device;
import com.itaicuker.unimot.viewModels.DeviceViewModel;

public class RemoteFragment extends Fragment
{
    private static final String TAG = "RemoteFragment";
    NavController navController;
    FragmentRemoteBinding binding;

    DeviceViewModel deviceViewModel;
    Device device;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRemoteBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        String uId = getArguments().getString("uId");

        deviceViewModel = new ViewModelProvider(this).get(DeviceViewModel.class);
        deviceViewModel.getDeviceMutableLiveData(uId).observe(this, device -> {
            Log.d(TAG, device.toString());
            this.device = device;
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            actionBar.setLogo(device.getDeviceType().getIcon());
            actionBar.setTitle(device.getName());

        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.remote_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
}
