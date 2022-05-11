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

/**
 * The Device list adapter.
 */
public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceHolder> {

    private static final String TAG = "UNIMOT: " + DeviceListAdapter.class.getSimpleName();

    /**
     * The Device list.
     */
    List<Device> deviceList;

    /**
     * Instantiates a new Device list adapter.
     *
     * @param deviceList the device list
     */
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

    /**
     * The type Device holder.
     */
    public static class DeviceHolder extends RecyclerView.ViewHolder {
        /**
         * The Binding.
         */
        public DeviceCardBinding binding;
        /**
         * The Id.
         */
        public String id;  // Firestore unique id of device

        /**
         * Instantiates a new Device holder.
         *
         * @param binding the binding
         */
        public DeviceHolder(@NonNull DeviceCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            //on click for device card
            View.OnClickListener deviceClickListener = v -> {
                NavController navController = Navigation.findNavController(v);
                Bundle bundle = new Bundle();
                bundle.putString("id", id);
                navController.navigate(R.id.action_mainFragment_to_DeviceFragment, bundle);
            };
            binding.btnDevice.setOnClickListener(deviceClickListener);
        }

        /**
         * Bind device to view using dataBinding
         *
         * @param device the device to bind
         */
        public void bind(@NonNull Device device) {
            id = device.getId();
            binding.setVariable(BR.device, device);
            binding.executePendingBindings();
        }
    }
}
