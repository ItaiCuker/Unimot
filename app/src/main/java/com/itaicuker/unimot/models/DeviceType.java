package com.itaicuker.unimot.models;


import androidx.annotation.DrawableRes;
import androidx.databinding.BindingAdapter;

import com.google.android.material.button.MaterialButton;
import com.itaicuker.unimot.R;

/**
 * Enumeration of {@link Device} types.
 */
public enum DeviceType {
    AC(R.drawable.device_ac),
    APPLE_TV(R.drawable.device_apple_tv),
    TV(R.drawable.device_tv),
    SPEAKER(R.drawable.device_speaker),
    PROJECTOR(R.drawable.device_projector);

    /**
     * the resource of icon
     */
    private final @DrawableRes int icon;

    /**
     * @param icon resource id of icon
     */
    DeviceType(@DrawableRes int icon) {

        this.icon = icon;
    }

    /**
     * @return resource id of icon
     */
    public int getIcon() {
        return icon;
    }

    @BindingAdapter("dynamicIcon")
    public static void setDynamicIcon(MaterialButton button, @DrawableRes int icon) {
        button.setIconResource(icon);
    }
}