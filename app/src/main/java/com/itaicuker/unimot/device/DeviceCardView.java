package com.itaicuker.unimot.device;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.google.android.material.card.MaterialCardView;
import com.itaicuker.unimot.databinding.DeviceCardBinding;


public class DeviceCardView extends MaterialCardView
{
    DeviceCardBinding mBinding;

    public DeviceCardView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        mBinding = DeviceCardBinding.inflate(inflater, this, true);
    }
}
