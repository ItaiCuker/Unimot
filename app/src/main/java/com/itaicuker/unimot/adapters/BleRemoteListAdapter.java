// Copyright 2020 Espressif Systems (Shanghai) PTE LTD
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.itaicuker.unimot.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.itaicuker.unimot.R;
import com.itaicuker.unimot.models.Remote;

import java.util.ArrayList;

/**
 * The ArrayAdapter<Remote> Ble remote list adapter.
 */
public class BleRemoteListAdapter extends ArrayAdapter<Remote> {

    private final Context context;
    private final ArrayList<Remote> bluetoothRemotes;

    /**
     * Instantiates a new Ble remote list adapter.
     *
     * @param context          the context
     * @param resource         the resource
     * @param bluetoothRemotes the bluetooth remotes list
     */
    public BleRemoteListAdapter(Context context, int resource, ArrayList<Remote> bluetoothRemotes) {
        super(context, resource, bluetoothRemotes);
        this.context = context;
        this.bluetoothRemotes = bluetoothRemotes;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        Remote remote = bluetoothRemotes.get(position);

        //get the inflater and inflate the XML layout for each item
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_ble_scan, null);

        TextView bleDeviceNameText = view.findViewById(R.id.tvBleRemoteName);
        bleDeviceNameText.setText(remote.getId());

        return view;
    }

}
