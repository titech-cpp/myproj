package com.example.bt_transmission;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

//import android.support.*;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.sql.Struct;
import java.util.ArrayList;
import java.util.Set;

class BTindex{
    public static BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
}


public class MainActivity extends AppCompatActivity {
//Permission Check
//    if (ContextCompat.checkSelfPermission(MainActivity, Manifest.permission.BLUETOOTH)
//            != PackageManager.PERMISSION_GRANTED) {
    // Permission is not granted
//    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = (TextView) findViewById(R.id.textView);
        //if connected
        textView.setText(R.string.connected);
        if (BTindex.bluetoothAdapter == null) {
            //Device doesn"t support Bluetooth
            textView.setText(R.string.unsupport);
            return;
        } else {
            textView.setText(R.string.support);
        }
        if (!BTindex.bluetoothAdapter.isEnabled()) {
            /* if false */
            int REQUEST_ENABLE_BT = 100;
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        Set<BluetoothDevice> pairedDevices = BTindex.bluetoothAdapter.getBondedDevices();
        int num = 0;
        String[] deviceList;
        deviceList = new String[]{"o", "o"};
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                deviceList[num] = deviceName +"\t"+ deviceHardwareAddress;
                num++;
            }
        }
        TextView textView2 = (TextView) findViewById(R.id.statement);
        String viewText= "These are paired Devices.\n";
        for (int i=0;i<=deviceList.length;i++){
            viewText = viewText + '\n';
        }
        textView2.setText(viewText);
    }
}

//    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
/*        if (BTindex.bluetoothAdapter == null) {
        // Device doesn't support Bluetooth
    }*/
/*        if (BTindex.bluetoothAdapter != null ) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        int REQUEST_ENABLE_BT;
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }*/

