package com.example.bt_transmission;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothHidDeviceAppQosSettings;
import android.bluetooth.BluetoothHidDeviceAppSdpSettings;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.bluetooth.BluetoothHidDevice.*;
import static android.widget.AdapterView.*;
import static com.example.bt_transmission.BTindex.MY_UUID;
import static com.example.bt_transmission.BTindex.PSM;
import static com.example.bt_transmission.BTindex.Transmit;
import static com.example.bt_transmission.BTindex.bluetoothAdapter;
import static com.example.bt_transmission.BTindex.bluetoothHidDevice;
import static com.example.bt_transmission.BTindex.bluetoothSocket;
import static com.example.bt_transmission.BTindex.targetMACaddress;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

class BTindex {
    public static BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public static BluetoothSocket bluetoothSocket = null;
    public static BluetoothHidDevice bluetoothHidDevice = null;
    public static UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static Boolean Transmit = FALSE;
    public static int PSM = 0013;//HID_Interrupt on https://www.bluetooth.com/ja-jp/specifications/assigned-numbers/logical-link-control/
    public static String targetMACaddress = "00:00:00:00:00:00";
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
        {
            TextView textView = findViewById(R.id.status);
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
                final Spinner spinner = (Spinner) findViewById(R.id.Connection_Spinner);
                setSpinner(spinner,pairedDevices);
                spinner.setOnItemSelectedListener(
                        new OnItemSelectedListener(){
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view,int position, long id){
                                NameAddressPair pair = (NameAddressPair) spinner.getSelectedItem();
                                targetMACaddress = pair.getAddress();
                                Toast.makeText(parent.getContext(),pair.getAddress() +"is selected",Toast.LENGTH_SHORT).show();
                            };
                            public void onNothingSelected(AdapterView<?> adapter){}
                        }
                );
            }

        }
        {
            CompoundButton button_Switch_SPP = findViewById(R.id.connect_switch1);
            CompoundButton button_Switch_HID = findViewById(R.id.connect_switch2);
            //SPP connection
            button_Switch_SPP.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    try {
                        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(targetMACaddress);
                        bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (bluetoothSocket != null){
                        if (isChecked){
                            try {
                                bluetoothSocket.connect();
                                Transmit = TRUE;
                                Toast.makeText(mainActivity,"Hello SPP",Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }else{
                            try {
                                bluetoothSocket.close();
                                Transmit = FALSE;
                                bluetoothSocket = null;
                                Toast.makeText(mainActivity,"Good bye",Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
            //HID connection
            button_Switch_HID.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                    try {
                        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(targetMACaddress);
                        bluetoothSocket = bluetoothDevice.createL2capChannel(PSM);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //ここからbluetoothHidDevice.getProfileProxyの設定のための変数
                    BluetoothProfile.ServiceListener listener = new BluetoothProfile.ServiceListener() {
                        @Override
                        public void onServiceConnected(int profile, BluetoothProfile proxy) {
                            Toast.makeText(mainActivity, "Connected" + profile, Toast.LENGTH_SHORT).show();
                            if (profile == BluetoothProfile.HID_DEVICE){
                                BTindex.bluetoothHidDevice = (BluetoothHidDevice) proxy;
                            }
                        }

                        @Override
                        public void onServiceDisconnected(int profile) {
                            Toast.makeText(mainActivity, "Good bye" + profile, Toast.LENGTH_SHORT).show();
                            if (profile == BluetoothHidDevice.HID_DEVICE){
                                bluetoothHidDevice = null;
                            }
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
                        final TextView statusText = findViewById(R.id.status);
                        Runnable commands = new Runnable() {
                            @Override
                            public void run() {
                                    statusText.setText("HID on " + LocalTime.now());
                            }
                        };

                        ExecutorService executor = Executors.newFixedThreadPool(10);
                        executor.submit(commands);

                            /*todo 下の行の修正から
                               https://developer.android.com/reference/android/bluetooth/BluetoothHidDeviceAppSdpSettings
                               https://developer.android.com/reference/android/bluetooth/BluetoothHidDevice#registerApp(android.bluetooth.BluetoothHidDeviceAppSdpSettings,%20android.bluetooth.BluetoothHidDeviceAppQosSettings,%20android.bluetooth.BluetoothHidDeviceAppQosSettings,%20java.util.concurrent.Executor,%20android.bluetooth.BluetoothHidDevice.Callback)
                            */
                        BluetoothHidDeviceAppQosSettings qosSettings = new BluetoothHidDeviceAppQosSettings(BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT, 0, 0, 0, BluetoothHidDeviceAppQosSettings.MAX, BluetoothHidDeviceAppQosSettings.MAX);
                        // all default
                        if (bluetoothHidDevice == null){
                            Toast.makeText(mainActivity,"NULL",Toast.LENGTH_SHORT).show();
                        }else{

                            BTindex.bluetoothHidDevice.registerApp(Sdp_Setting, qosSettings, qosSettings, executor, callback);
                            executor.execute(commands);

                        }

                        try {
                            bluetoothSocket.connect();
                            Transmit = TRUE;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else try {
                        bluetoothSocket.close();
                        Transmit = FALSE;
                        bluetoothSocket = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
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

                        if (Transmit){
                            try {
                                OutputStream btOutput = bluetoothSocket.getOutputStream();
                                String strTmp = "Hello,world!!!";
                                byte[] strByteArray = strTmp.getBytes();
                                btOutput.write(strByteArray);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                //set Right-Click moving
                button_Click_R.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        strTmp.append("R");
                        textView3.setText(strTmp);

                        if (TRUE){
                            try {
                                OutputStream btOutput = bluetoothSocket.getOutputStream();
                                String strTmp = "Hello,world!!!";
                                byte[] strByteArray = strTmp.getBytes();
                                btOutput.write(strByteArray);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
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
        };

    }
    private void setSpinner(Spinner spinner, Set<BluetoothDevice> arr){
        // There are paired devices. Get the name and address of each paired device.
        List<NameAddressPair> deviceInfoList = new ArrayList<>();
        for (BluetoothDevice device : arr) {
            NameAddressPair pair = new NameAddressPair(device.getName(),device.getAddress());
            deviceInfoList.add(pair);
        }
        List<String> deviceNameList = new ArrayList<>();
        NameAddressPairArrayAdapter adapter = new NameAddressPairArrayAdapter(this,android.R.layout.simple_spinner_item, deviceInfoList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    };
    public class NameAddressPair extends Pair<String,String> {
      public NameAddressPair(String name, String address){
          super(name,address);
      }
      public String getName(){
          return super.first;
      }
      public String getAddress(){
          return super.second;
      }
    };
    public class NameAddressPairArrayAdapter extends ArrayAdapter<NameAddressPair>{
        public NameAddressPairArrayAdapter(Context context, int textViewResourceId, List<NameAddressPair> list){
            super(context, textViewResourceId, list);
        }
        public NameAddressPairArrayAdapter(Context context, int textViewResourceId){
            super(context, textViewResourceId);
        }

        public View getView(int position, View convertView, ViewGroup parent){
            TextView view = (TextView) super.getView(position,convertView,parent);
            view.setText(getItem(position).getName());
            return view;
        }

        public int getPosition(String name){
            int position = -1;
            for (int i=0; i < this.getCount();i++){
                if (this.getItem(i).getName() == name){
                    position = i;
                    break;
                }
            }
            return position;
        }

        public String getAddress(String name){
            int position = 0;
            for (int i=0; i < this.getCount();i++){
                if (this.getItem(i).getName() == name){
                    position = i;
                    break;
                }
            }
            return this.getItem(position).getAddress();
        }
    }
}