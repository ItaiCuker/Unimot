package com.itaicuker.unimot.models;


import com.itaicuker.unimot.R;

/**
 * Enumeration of {@link Device} types.
 * User can add types to enum.
 */
public enum DeviceType {
    AC(R.drawable.device_ac),
    APPLE_TV(R.drawable.device_apple_tv),
    TV(R.drawable.device_tv),
    PROJECTOR(R.drawable.device_projector);

    private final int icon;

    /**
     * @param icon resource id of icon
     */
    DeviceType(int icon) {

        this.icon = icon;
    }

    public int getIcon() {
        return icon;
    }
}