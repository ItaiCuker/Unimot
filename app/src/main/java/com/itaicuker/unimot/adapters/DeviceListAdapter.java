package com.itaicuker.unimot.adapters;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.itaicuker.unimot.BR;
import com.itaicuker.unimot.R;
import com.itaicuker.unimot.databinding.DeviceCardBinding;
import com.itaicuker.unimot.models.Device;

import java.util.List;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceHolder> {

    private static final String TAG = "DeviceListAdapter";

    List<Device> deviceList;

    public DeviceListAdapter(List<Device> deviceList) {
        this.deviceList = deviceList;
    }

    @NonNull
    @Override
    public DeviceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        DeviceCardBinding binding = DeviceCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new DeviceHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceHolder holder, int position) {
        Log.d(TAG, deviceList.get(position).toString());
        holder.bind(deviceList.get(position));
    }

    @Override
    public int getItemCount() {
        return deviceList == null ? 0 : deviceList.size();
    }

    public static class DeviceHolder extends RecyclerView.ViewHolder {
        public DeviceCardBinding binding;
        public String uId;  //firestore unique id of device

        public DeviceHolder(DeviceCardBinding binding) {
            super(binding.getRoot());
            // Define click listener for the DeviceHolder's View
            this.binding = binding;
            binding.btnDevice.setOnClickListener(deviceClickListener);
        }

        public void bind(Device device) {
            uId = device.getuId();
            binding.setVariable(BR.device, device);
            binding.executePendingBindings();
        }

        private View.OnClickListener deviceClickListener = v -> {
            NavController navController = Navigation.findNavController(v);
            Bundle bundle = new Bundle();
            bundle.putString("uId", uId);
            navController.navigate(R.id.action_mainFragment_to_remoteFragment, bundle);
        };

        public DeviceCardBinding getBinding() {
            return binding;
        }
    }
}
