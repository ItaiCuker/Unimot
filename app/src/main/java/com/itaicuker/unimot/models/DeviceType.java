package com.itaicuker.unimot.models;


import android.content.Context;
import android.graphics.drawable.Drawable;

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
    public Drawable getIcon(Context context) {
        return context.getDrawable(icon);
    }
}