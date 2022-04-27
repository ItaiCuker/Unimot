package com.itaicuker.unimot.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.databinding.BindingMethod;
import androidx.databinding.BindingMethods;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.itaicuker.unimot.BR;
import com.itaicuker.unimot.R;
import com.itaicuker.unimot.databinding.DeviceCardBinding;
import com.itaicuker.unimot.models.Device;

import java.util.List;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {

    private final List<Device> deviceList;

    public DeviceListAdapter(List<Device> deviceList) {
        this.deviceList = deviceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        DeviceCardBinding binding = DeviceCardBinding.inflate(    //switch from DataBindingUtil?
                LayoutInflater.from(parent.getContext()), parent, false);
        
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Device device = deviceList.get(position);
        holder.bind(device);
        //TODO: add click listener here
    }

    @Override
    public int getItemCount() {
        return deviceList == null ? 0 : deviceList.size();
    }

    /**
     * custom viewHolder for material card
     */
    @BindingMethods({
            @BindingMethod(type = android.widget.ImageView.class,
                    attribute = "app:srcCompat",
                    method = "setImageDrawable") })
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public DeviceCardBinding binding;

        public ViewHolder(DeviceCardBinding binding) {
            super(binding.getRoot());
            // Define click listener for the ViewHolder's View
            this.binding = binding;
        }

        public void bind(Device device) {
            binding.setVariable(BR.device, device);
            binding.executePendingBindings();
        }

        //TODO: add click logic

        public DeviceCardBinding getBinding() {
            return binding;
        }
    }
}
