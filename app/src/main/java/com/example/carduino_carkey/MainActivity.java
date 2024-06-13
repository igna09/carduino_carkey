package com.example.carduino_carkey;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.Manifest;

public class MainActivity extends AppCompatActivity {
    private AppCompatActivity activity;
    private boolean scanning;
    private BluetoothLeScanner bluetoothLeScanner;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bluetoothLeScanner = getSystemService(BluetoothAdapter.class).getBluetoothLeScanner();
        activity = this;
    }

    private void scanLeDevice() {
        // Device scan callback.
        ScanCallback leScanCallback =
                new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        super.onScanResult(callbackType, result);
                        Log.d("MainActivity", result.toString());

                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            ActivityCompat.requestPermissions(
                                    activity,
                                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                                    0
                            );
                            return;
                        }
                        if (result != null && result.getDevice() != null && result.getDevice().getName().equals("ESP32")) {
                            result.getDevice().connectGatt(getApplicationContext(), true, new BluetoothGattCallback() {
                                @Override
                                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                                    super.onConnectionStateChange(gatt, status, newState);
                                    Log.d("MainActivity", gatt.toString());
                                }
                            });
                        }
                    }
                };
        Handler handler = new Handler();
        if (!scanning) {
            // Stops scanning after a predefined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        ActivityCompat.requestPermissions(
                                activity,
                                new String[]{Manifest.permission.BLUETOOTH_SCAN},
                                0
                        );
                        return;
                    }
                    bluetoothLeScanner.stopScan(leScanCallback);
                }
            }, SCAN_PERIOD);

            scanning = true;
            bluetoothLeScanner.startScan(leScanCallback);
        } else {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }
}