package com.itaicuker.unimot.models;


import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;

import com.google.android.material.button.MaterialButton;
import com.itaicuker.unimot.R;

/**
 * Enumeration of {@link Device} types.
 */
public enum DeviceType {
    /**
     * Ac device type.
     */
    AC(R.drawable.device_ac),
    /**
     * Tv device type.
     */
    TV(R.drawable.device_tv),
    /**
     * Speaker device type.
     */
    SPEAKER(R.drawable.device_speaker),
    /**
     * Projector device type.
     */
    PROJECTOR(R.drawable.device_projector);

    /**
     * the resource of icon
     */
    private final @DrawableRes
    int icon;

    /**
     * @param icon resource id of icon
     */
    DeviceType(@DrawableRes int icon) {

        this.icon = icon;
    }

    /**
     * Sets dynamic icon.
     *
     * @param button the button
     * @param icon   the icon
     */
    @BindingAdapter("dynamicIcon")
    public static void setDynamicIcon(@NonNull MaterialButton button, @DrawableRes int icon) {
        button.setIconResource(icon);
    }

    /**
     * Gets icon.
     *
     * @return resource id of icon
     */
    @NonNull
    public int getIcon() {
        return icon;
    }
}