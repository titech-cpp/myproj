package com.example.bt_transmission;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothHidDeviceAppQosSettings;
import android.bluetooth.BluetoothHidDeviceAppSdpSettings;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

//import android.support.*;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.zip.CheckedOutputStream;

import static android.bluetooth.BluetoothHidDevice.*;
import static android.bluetooth.BluetoothHidDevice.SUBCLASS1_MOUSE;
import static android.content.ContentValues.TAG;
import static com.example.bt_transmission.BTindex.MY_UUID;
import static com.example.bt_transmission.BTindex.PSM;
import static com.example.bt_transmission.BTindex.bluetoothAdapter;
import static com.example.bt_transmission.BTindex.bluetoothDevice;
//import static com.example.bt_transmission.BTindex.bluetoothHidDevice;
import static com.example.bt_transmission.BTindex.bluetoothSocket;
import static com.example.bt_transmission.BTindex.strTmp;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

class BTindex {
    public static BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public static BluetoothSocket bluetoothSocket;
    public static BluetoothDevice bluetoothDevice;
    public static BluetoothHidDevice bluetoothHidDevice;
    public static UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
//    public static UUID HIDP_UUID = UUID.fromString("00000011-0000-1000-8000-00805F9B34FB");
    public static int PSM = 0013;//HID_Interrupt on https://www.bluetooth.com/ja-jp/specifications/assigned-numbers/logical-link-control/
    public static StringBuffer strTmp = new StringBuffer();
    public static  final String targetMACaddress = "3C:91:80:5C:1B:7C";
    public static final byte[] discriptor = new byte[]{
        //HID discriptor on https://www.usb.org/sites/default/files/hid1_11.pdf
            0x09, // bLength
            0x21, // bDescriptorType - HID
            0x11, 0x01, // bcdHID release no.
            0x00, // bCountryCode
            0x01, // bNumDescriptors (min 1)
            0x22, // bDescriptorType - Report
            0x32, 0x00, // wDescriptorLength (48)todo???

            // Report descriptor
            0x05, 0x01,        // USAGE_PAGE (Generic Desktop)
            0x09, 0x02,        // USAGE (Mouse)
            (byte) 0xa1, 0x01, // COLLECTION (Application)
            0x09, 0x01,        //   USAGE (Pointer) <<
            (byte) 0xa1, 0x00, //   COLLECTION (Physical)
            0x05, 0x09,        //     USAGE_PAGE (Button)
            0x19, 0x01,        //     USAGE_MINIMUM (Button 1)
            0x29, 0x03,        //     USAGE_MAXIMUM (Button 3)
            0x15, 0x00,        //     LOGICAL_MINIMUM (0)
            0x25, 0x01,        //     LOGICAL_MAXIMUM (1)
            (byte) 0x95, 0x03, //     REPORT_COUNT (3) <<change down
            0x75, 0x01,        //     REPORT_SIZE (1) <<change up
            (byte) 0x81, 0x02, //     INPUT 3bit (Data,Variable,Absolute)
            (byte) 0x95, 0x01, //     REPORT_COUNT (1) <<
            0x75, 0x05,        //     REPORT_SIZE (5) <<
            (byte) 0x81, 0x01, //     INPUT 5bit (Constant,Variable,Absolute)
            0x05, 0x01,        //     USAGE_PAGE (Generic Desktop)
            0x09, 0x30,        //     USAGE (X)
            0x09, 0x31,        //     USAGE (Y)
            0x15, (byte) 0x81, //     LOGICAL_MINIMUM (-127)
            0x25, 0x7f,        //     LOGICAL_MAXIMUM (127)
            0x75, 0x08,        //     REPORT_SIZE (8)
            (byte) 0x95, 0x02, //     REPORT_COUNT (2)
            (byte) 0x81, 0x06, //     INPUT 2 position bytes x& y(Data,Variavle,Absolute)
            (byte) 0xc0,       //   END_COLLECTION
            (byte) 0xc0        // END_COLLECTION


    };
}
public class MainActivity extends AppCompatActivity  {
//Permission Check
//    if (ContextCompat.checkSelfPermission(MainActivity, Manifest.permission.BLUETOOTH)
//            != PackageManager.PERMISSION_GRANTED) {
    // Permission is not granted
//    }

    final MainActivity mainActivity = this;
    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        /*接続部門．動作チェック済み*/
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //setting receicer
        final BroadcastReceiver receiver = new BroadcastReceiver() {
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
            String deviceHardwareAddress = BTindex.targetMACaddress;
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

                    //ここからbluetoothHidDevice.getProfileProxyの設定のための変数
                    BluetoothProfile.ServiceListener listener = new BluetoothProfile.ServiceListener() {
                        @Override
                        public void onServiceConnected(int profile, BluetoothProfile proxy) {
                            Context context = getApplicationContext();
                            CharSequence text = "Connected on "+ profile;
                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        }

                        @Override
                        public void onServiceDisconnected(int profile) {
                            Context context = getApplicationContext();
                            CharSequence text = "Goodbye!!";
                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();

                        }
                    };
                    Callback callback = new Callback() {
                        @Override
                        public void onGetReport(BluetoothDevice device, byte type, byte id, int bufferSize) {
                            super.onGetReport(device, type, id, bufferSize);
                        }
                    };
                    //ここまでbluetoothHidDevice.getProfileProxyのための変数設定
                    bluetoothAdapter.getProfileProxy(mainActivity, listener, BluetoothProfile.HID_DEVICE);
                    if (isChecked) {
                            BluetoothHidDeviceAppSdpSettings Sdp_Setting = new BluetoothHidDeviceAppSdpSettings("Real_BTMouse", "Virtual mouse on Real", "Programmer_Fish", SUBCLASS1_MOUSE, BTindex.discriptor);
                            //memo Sdp_settingの値は直った
                            Executor executor = new Executor() {
                                @Override
                                public void execute(Runnable run) {
                                    public void run() {
                                        //todo たぶんここにボタンを押した時のhid_deviceの動き方を記述すれば良さそう 0615
                                    }
                                }
                            };
                            /*todo 下の行の修正から
                               https://developer.android.com/reference/android/bluetooth/BluetoothHidDeviceAppSdpSettings
                               https://developer.android.com/reference/android/bluetooth/BluetoothHidDevice#registerApp(android.bluetooth.BluetoothHidDeviceAppSdpSettings,%20android.bluetooth.BluetoothHidDeviceAppQosSettings,%20android.bluetooth.BluetoothHidDeviceAppQosSettings,%20java.util.concurrent.Executor,%20android.bluetooth.BluetoothHidDevice.Callback)
                            */

                            BTindex.bluetoothHidDevice.registerApp(Sdp_Setting, null, null,executor,callback);

                                try {
                                    bluetoothSocket.connect();
                                } catch (IOException e) {
                                    e.printStackTrace();
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
                    }
                }
            });
        };
    }
}