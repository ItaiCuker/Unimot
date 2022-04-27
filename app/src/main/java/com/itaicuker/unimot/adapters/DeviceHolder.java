package com.itaicuker.unimot.adapters;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.itaicuker.unimot.BR;
import com.itaicuker.unimot.R;
import com.itaicuker.unimot.databinding.DeviceCardBinding;
import com.itaicuker.unimot.models.Device;

/**
 * custom viewHolder for material card
 */
public class DeviceHolder extends RecyclerView.ViewHolder {
    public DeviceCardBinding binding;

    public DeviceHolder(DeviceCardBinding binding) {
        super(binding.getRoot());
        // Define click listener for the DeviceHolder's View
        this.binding = binding;
        binding.btnDevice.setOnClickListener(deviceClickListener);
    }

    public void bind(Device device) {
        binding.setVariable(BR.device, device);
        binding.executePendingBindings();
    }

    private View.OnClickListener deviceClickListener = v -> {
        //TODO: go to device ui screen
        NavController navController = Navigation.findNavController(v);
        navController.navigate(R.id.action_mainFragment_to_remoteFragment);
    };

    public DeviceCardBinding getBinding() {
        return binding;
    }
}
