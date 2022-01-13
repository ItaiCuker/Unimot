package com.itaicuker.unimot.models;


import com.itaicuker.unimot.R;

/**
 * Enumeration of {@link Device} types.
 */
public enum DeviceType {
    AC(R.drawable.device_ac),
    APPLE_TV(R.drawable.device_apple_tv),
    TV(R.drawable.device_tv),
    PROJECTOR(R.drawable.device_projector);

    /**
     * the resource of icon
     */
    private final int icon;

    /**
     * @param icon resource id of icon
     */
    DeviceType(int icon) {

        this.icon = icon;
    }

    /**
     * @return resource id of icon
     */
    public int getIcon() {
        return icon;
    }
}