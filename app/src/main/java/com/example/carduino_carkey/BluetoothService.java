package com.example.carduino_carkey;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

public class BluetoothService extends Service {
    private static final String TAG = "BluetoothService";
    private BluetoothAdapter bluetoothAdapter;
    private BroadcastReceiver broadcastReceiver;
    public BluetoothService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();

        broadcastReceiver = new BroadcastReceiver() {
            public void onReceive (Context context, Intent intent) {
                String action = intent.getAction();

                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
                        case BluetoothAdapter.STATE_OFF:
                            // Bluetooth is disconnected, do handling here
                            break;
                        case BluetoothAdapter.STATE_ON:
                            connectToESP();
                            break;
                    }
                }
            }
        };
        getApplicationContext().registerReceiver(broadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        final String CHANNELID = "Foreground Service ID";
        NotificationChannel channel = new NotificationChannel(
                CHANNELID,
                CHANNELID,
                NotificationManager.IMPORTANCE_MIN
        );

        Intent stopIntent = new Intent(this, BluetoothService.class);
        stopIntent.setAction("STOP_FOREGROUND");

        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNELID)
                .setContentText("Service is running, expand me to stop")
                .setContentTitle("Carduino service")
//                .setSmallIcon(R.drawable.baseline_directions_car_24)
                // Add the cancel action to the notification which can
                // be used to cancel the worker
                .addAction(android.R.drawable.ic_delete, "STOP", PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        startForeground(1001, notification.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);

        Toast.makeText(this, "BluetoothService Started", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onStart");

        connectToESP();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    public int connectToESP() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "BLUETOOTH_CONNECT not granted", Toast.LENGTH_LONG).show();
            return START_STICKY;
        }
        BluetoothDevice bluetoothDevice = bluetoothAdapter.getBondedDevices().stream().filter(e -> e.getName().equals("ESP32")).findFirst().orElse(null);

        if (bluetoothDevice != null) {

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
//                    ActivityCompat.requestPermissions(
//                            context.getApplicationContext().getAc,
//                            new String[]{Manifest.permission.BLUETOOTH_CONNECT},
//                            0
//                    );
                Toast.makeText(this, "BLUETOOTH_CONNECT not granted", Toast.LENGTH_LONG).show();
                return START_STICKY;
            }
            bluetoothDevice.connectGatt(getApplicationContext(), true, new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    super.onConnectionStateChange(gatt, status, newState);
                    Log.d("MainActivity", gatt.toString());
                }
            });
        }
        return START_STICKY;
    }
}