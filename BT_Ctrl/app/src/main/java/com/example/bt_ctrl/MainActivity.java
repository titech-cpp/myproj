package com.example.bt_ctrl;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothHidDeviceAppQosSettings;
import android.bluetooth.BluetoothHidDeviceAppSdpSettings;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.bluetooth.BluetoothHidDevice.*;
import static com.example.bt_ctrl.BTindex.PSM;
import static com.example.bt_ctrl.BTindex.Transmit_HID;
import static com.example.bt_ctrl.BTindex.bluetoothAdapter;
import static com.example.bt_ctrl.BTindex.bluetoothDevice;
import static com.example.bt_ctrl.BTindex.bluetoothHidDevice;
import static com.example.bt_ctrl.BTindex.bluetoothSocket;
import static com.example.bt_ctrl.BTindex.mainActivity;
import static com.example.bt_ctrl.BTindex.targetMACaddress;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

class BTindex {
    public static BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public static BluetoothSocket bluetoothSocket = null;
    public static BluetoothHidDevice bluetoothHidDevice = null;
    public static BluetoothDevice bluetoothDevice = null;
    public static UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static Boolean Transmit_SPP = FALSE;
    public static Boolean Transmit_HID = FALSE;
    public static int PSM = 0013;//HID_Interrupt on https://www.bluetooth.com/ja-jp/specifications/assigned-numbers/logical-link-control/
    public static String targetMACaddress = "00:00:00:00:00:00";
    public static MainActivity mainActivity = null;
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
public class MainActivity extends AppCompatActivity {
    private bluetoothService myService = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
            TextView textView = findViewById(R.id.status);
            if (BTindex.bluetoothAdapter == null) {
                //Device doesn"t support Bluetooth
                textView.setText(R.string.unsupport);
                return;
            } else {
                //Device supports BT
                textView.setText(R.string.support);
            }}
            {
            if (!BTindex.bluetoothAdapter.isEnabled()) {
                /* if false */
                int REQUEST_ENABLE_BT = 100;
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {//show spinner
                Set<BluetoothDevice> pairedDevices = BTindex.bluetoothAdapter.getBondedDevices();
                final Spinner spinner = (Spinner) findViewById(R.id.Connection_Spinner);
                setSpinner(spinner,pairedDevices);
                spinner.setOnItemSelectedListener(
                        new AdapterView.OnItemSelectedListener(){
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
                                NameAddressPair pair = (NameAddressPair) spinner.getSelectedItem();
                                targetMACaddress = pair.getAddress();
                                viewInfo(pair.getAddress() +"is selected");
                            };
                            public void onNothingSelected(AdapterView<?> adapter){}
                        }
                );
            }final TextView statusText = findViewById(R.id.status);
        final Runnable commands = new Runnable() {
            @Override
            public void run() {
                statusText.setText("HID on " + LocalTime.now());
            }
        };

        final ExecutorService executor = Executors.newFixedThreadPool(10);
        final BluetoothHidDeviceAppQosSettings qosSettings = new BluetoothHidDeviceAppQosSettings(BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT, 0, 0, 0, BluetoothHidDeviceAppQosSettings.MAX, BluetoothHidDeviceAppQosSettings.MAX);
        // all default
        final Callback callback = new Callback() {
            @Override
            public void onGetReport(BluetoothDevice device, byte type, byte id, int bufferSize) {
                super.onGetReport(device, type, id, bufferSize);
            }
        };
            try {
                BTindex.bluetoothDevice = bluetoothAdapter.getRemoteDevice(targetMACaddress);
                bluetoothSocket = BTindex.bluetoothDevice.createL2capChannel(PSM);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //ここからbluetoothHidDevice.getProfileProxyの設定のための変数
        final BluetoothHidDeviceAppSdpSettings Sdp_Setting = new BluetoothHidDeviceAppSdpSettings("Real_BTMouse", "Virtual mouse on Real", "Programmer_Fish", SUBCLASS1_MOUSE, BTindex.discriptor);
            BluetoothProfile.ServiceListener listener = new BluetoothProfile.ServiceListener() {
                //@Override
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    viewInfo("HERE");
                    if (profile == BluetoothProfile.HID_DEVICE){//note: check HID device (or INPUT_HOST)
                        Log.v("test","LINE178");//TODO:not come here
                        viewInfo("Connected" + profile);
                        BTindex.bluetoothHidDevice = (BluetoothHidDevice) proxy;
                        BTindex.bluetoothHidDevice.registerApp(Sdp_Setting, qosSettings, qosSettings, executor, callback);
                        executor.execute(commands);
                    }
                }
                //@Override
                public void onServiceDisconnected(int profile) {
                    viewInfo("Good bye" + profile);
                    if (profile == BluetoothHidDevice.HID_DEVICE){
                        bluetoothHidDevice = null;
                    }
                }
            };
            //ここまでbluetoothHidDevice.getProfileProxyのための変数設定
            bluetoothAdapter.getProfileProxy(this, listener, BluetoothProfile.HID_DEVICE);
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.v("test","LINE198");//OK
                bluetoothService.LocalBinder binder = (bluetoothService.LocalBinder) service;
                myService = binder.getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                myService = null;
            }
        };
        Log.i("BluetoothProfile",Integer.toString(bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HID_DEVICE)));
                //todo bindServiceについて
                // https://developer.android.com/reference/android/bluetooth/BluetoothHidDeviceAppSdpSettings
                // https://developer.android.com/reference/android/bluetooth/BluetoothHidDevice#registerApp(android.bluetooth.BluetoothHidDeviceAppSdpSettings,%20android.bluetooth.BluetoothHidDeviceAppQosSettings,%20android.bluetooth.BluetoothHidDeviceAppQosSettings,%20java.util.concurrent.Executor,%20android.bluetooth.BluetoothHidDevice.Callback)
        try {
            Intent intent = new Intent(MainActivity.this,bluetoothService.class);
            bindService(intent,serviceConnection,Context.BIND_AUTO_CREATE);
        }catch (Exception e) {
            e.printStackTrace();
        }

        if (bluetoothHidDevice == null) {
            viewInfo("NULL");
        }else {
            BTindex.bluetoothHidDevice.registerApp(Sdp_Setting, qosSettings, qosSettings, executor, callback);
            executor.execute(commands);
            bluetoothHidDevice.connect(bluetoothDevice);
            Transmit_HID = TRUE;
        }
                // todo : BluetoothService Check

        final Button button_send = findViewById(R.id.btnSend);
            button_send.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    int number = myService.getNum();
                    viewInfo(Integer.toString(number));
                }
            });

    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        bluetoothAdapter.closeProfileProxy(BluetoothHidDevice.HID_DEVICE,bluetoothHidDevice);
        try {
            bluetoothSocket.close();
            Transmit_HID = FALSE;
            bluetoothSocket = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void setSpinner(Spinner spinner, Set<BluetoothDevice> arr){
        // There are paired devices. Get the name and address of each paired device.
        List<NameAddressPair> deviceInfoList = new ArrayList<>();
        for (BluetoothDevice device : arr) {
            NameAddressPair pair = new NameAddressPair(device.getName(), device.getAddress());
            deviceInfoList.add(pair);
        }
        List<String> deviceNameList = new ArrayList<>();
        NameAddressPairArrayAdapter adapter = new NameAddressPairArrayAdapter(this, android.R.layout.simple_spinner_item, deviceInfoList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
    private static class NameAddressPair extends Pair<String,String> {
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
    private static class NameAddressPairArrayAdapter extends ArrayAdapter<NameAddressPair> {
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
    private void viewInfo(String str){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }
}
