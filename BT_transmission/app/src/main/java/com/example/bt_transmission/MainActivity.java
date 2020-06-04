package com.example.bt_transmission;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothHidDeviceAppQosSettings;
import android.bluetooth.BluetoothHidDeviceAppSdpSettings;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;

//import android.support.*;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.zip.CheckedOutputStream;

import static android.bluetooth.BluetoothHidDevice.SUBCLASS1_MOUSE;
import static android.content.ContentValues.TAG;
import static com.example.bt_transmission.BTindex.MY_UUID;
import static com.example.bt_transmission.BTindex.PSM;
import static com.example.bt_transmission.BTindex.bluetoothAdapter;
import static com.example.bt_transmission.BTindex.bluetoothDevice;
import static com.example.bt_transmission.BTindex.bluetoothSocket;
import static com.example.bt_transmission.BTindex.strTmp;
import static java.lang.Boolean.TRUE;

class BTindex {
    public static BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public static BluetoothSocket bluetoothSocket;
    public static BluetoothDevice bluetoothDevice;
    public static UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static UUID HIDP_UUID = UUID.fromString("00000011-0000-1000-8000-00805F9B34FB");
    public static int PSM = 0013;//HID_Interrupt on https://www.bluetooth.com/ja-jp/specifications/assigned-numbers/logical-link-control/
    public static StringBuffer strTmp = new StringBuffer();
}
//set Connection for Client
/*
class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    public ConnectThread(BluetoothDevice device) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;
        mmDevice = device;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        BTindex.bluetoothAdapter.cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {

                BTindex.strTmp.append("Here?");
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        //manageMyConnectedSocket(mmSocket);
    }
    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }
}
*/

//sample sending
/*
class MyBluetoothService {
    private static final String TAG = "MY_APP_DEBUG_TAG";
    private Handler handler; // handler that gets info from Bluetooth service

    // Defines several constants used when transmitting messages between the
    // service and the UI.
    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

        // ... (Add other message types here as needed.)
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = handler.obtainMessage(
                            MessageConstants.MESSAGE_READ, numBytes, -1,
                            mmBuffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
                Message writtenMsg = handler.obtainMessage(
                        MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                handler.sendMessage(writeErrorMsg);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}
*/
public class MainActivity extends AppCompatActivity  {
//Permission Check
//    if (ContextCompat.checkSelfPermission(MainActivity, Manifest.permission.BLUETOOTH)
//            != PackageManager.PERMISSION_GRANTED) {
    // Permission is not granted
//    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        /*接続部門．動作チェック済み*/
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        {
            TextView textView = findViewById(R.id.statement);
            if (BTindex.bluetoothAdapter == null) {
                //Device doesn"t support Bluetooth
                textView.setText(R.string.unsupport);
                return;
            } else {
                //Device supports BT
                textView.setText(R.string.support);
            }

            if (!BTindex.bluetoothAdapter.isEnabled()) {
                /* if false */
                int REQUEST_ENABLE_BT = 100;
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                Set<BluetoothDevice> pairedDevices = BTindex.bluetoothAdapter.getBondedDevices();
                TextView textView2 = (TextView) findViewById(R.id.statement);
                StringBuffer viewText = new StringBuffer("These are paired Devices.\n");
                if (pairedDevices.size() > 0) {
                    // There are paired devices. Get the name and address of each paired device.
                    for (BluetoothDevice device : pairedDevices) {
                        String deviceName = device.getName();
                        String deviceHardwareAddress = device.getAddress(); // MAC address
                        viewText.append(deviceName);
                        viewText.append("\t");
                        viewText.append(deviceHardwareAddress);
                        viewText.append("\n\t");
                        viewText.append(strTmp);
                        strTmp.delete(0, strTmp.length());
                    }
                    textView2.setText(viewText);
                }
            }

        }
        // sample sending operator　なんもわからん
        {
            String deviceHardwareAddress = "3C:91:80:5C:1B:7C";
            bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceHardwareAddress);
            CompoundButton button_Switch_SPP = findViewById(R.id.connect_switch1);
            CompoundButton button_Switch_HID = findViewById(R.id.connect_switch2);
            //SPP connection
            button_Switch_SPP.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    try {
                        bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (isChecked) {
                        try {
                            bluetoothSocket.connect();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            bluetoothSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            //HID connection
            button_Switch_HID.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    try {
                        bluetoothSocket = bluetoothDevice.createL2capChannel(PSM);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (isChecked){
                        try {
                        bluetoothSocket.connect();
                        byte BTMouse_class = SUBCLASS1_MOUSE;
                        byte[] Sdp_setting = new byte[1];
                        Sdp_setting[0] = (byte) 0;
                        Sdp_setting[1] = (byte) 1;
                        BluetoothHidDeviceAppSdpSettings Sdp_Setting = new BluetoothHidDeviceAppSdpSettings("ASUS_T00P_BTMouse","Virtual mouse on ASUS_T00P","Programmer_Fish",BTMouse_class,Sdp_setting);
                        //この上の行のパラメータたぶん違う todo 特にSdp_settingの値
                        // todo https://developer.android.com/reference/android/bluetooth/BluetoothHidDeviceAppSdpSettings
                            // todo https://developer.android.com/reference/android/bluetooth/BluetoothHidDevice#registerApp(android.bluetooth.BluetoothHidDeviceAppSdpSettings,%20android.bluetooth.BluetoothHidDeviceAppQosSettings,%20android.bluetooth.BluetoothHidDeviceAppQosSettings,%20java.util.concurrent.Executor,%20android.bluetooth.BluetoothHidDevice.Callback)
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else{
                    try {
                        bluetoothSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }});

        }

        /*入力部門*/
        {
            final TextView textView3 = findViewById(R.id.statement3);
            final StringBuffer strTmp = new StringBuffer();
            final Button button_Click_L = findViewById(R.id.button_Click_L);
            final Button button_Click_R = findViewById(R.id.button_Click_R);
            final Button button_go_D = findViewById(R.id.button_go_downward);
            final Button button_go_U = findViewById(R.id.button_go_upward);
            final Button button_go_L = findViewById(R.id.button_go_left);
            final Button button_go_R = findViewById(R.id.button_go_right);
            final Button button_wheel_U = findViewById(R.id.button_wheel_upward);
            final Button button_wheel_D = findViewById(R.id.button_wheel_downward);
            //set Left-Click moving
            button_Click_L.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    strTmp.append("L");
                    textView3.setText(strTmp);

                    try {
                        OutputStream btOutput = bluetoothSocket.getOutputStream();
                        String strTmp = "Hello,world!!!";
                        byte[] strByteArray = strTmp.getBytes();
                        btOutput.write(strByteArray);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            //set Right-Click moving
            button_Click_R.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    strTmp.append("R");
                    textView3.setText(strTmp);
                }
            });
            //set GoDown moving
            button_go_D.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    strTmp.append("↓");
                    textView3.setText(strTmp);
                }
            });
            //set GoUp moving
            button_go_U.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    strTmp.append("↑");
                    textView3.setText(strTmp);
                }
            });
            //set Go Left moving
            button_go_L.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    strTmp.append("←");
                    textView3.setText(strTmp);
                }
            });
            //set Go Right moving
            button_go_R.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    strTmp.append("→");
                    textView3.setText(strTmp);
                }
            });
            //set Wheel Up moving
            button_wheel_U.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    strTmp.append("▲");
                    textView3.setText(strTmp);
                }
            });
            //set Wheel Down moving
            button_wheel_D.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    strTmp.append("▼");
                    textView3.setText(strTmp);
                }
            });
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            TextView textView = findViewById(R.id.statement);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceMACAddress = device.getAddress();
                StringBuffer strTmp = new StringBuffer(deviceName);
                strTmp.append(deviceMACAddress);
                textView.setText(strTmp);
            }
        }
    };
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

