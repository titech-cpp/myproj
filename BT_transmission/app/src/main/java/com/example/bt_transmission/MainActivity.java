package com.example.bt_transmission;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

//import android.support.*;
import android.widget.TextView;

class BTindex {
    static BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
}

public class MainActivity extends AppCompatActivity {
/*Permission Check
    if (ContextCompat.checkSelfPermission(MainActivity, Manifest.permission.BLUETOOTH)
            != PackageManager.PERMISSION_GRANTED) {
        // Permission is not granted
    }
*/
//    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            TextView textView = findViewById(R.id.text);
            textView.setText("void setText (CharSequence text)");
        }
//    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
/*        if (BTindex.bluetoothAdapter == null) {
        // Device doesn't support Bluetooth
    }
        if (BTindex.bluetoothAdapter != null ) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        int REQUEST_ENABLE_BT;
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
*/
}
