package com.example.carduino_carkey;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class BluetoothServiceReceiver extends BroadcastReceiver {

    private static final String TAG = "BluetoothServiceReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent bluetoothServiceIntent = new Intent(context, BluetoothService.class);

//        if(intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
//
//        } else {
            context.startForegroundService(bluetoothServiceIntent);
//        }

        Log.i(TAG, "started");
    }
}