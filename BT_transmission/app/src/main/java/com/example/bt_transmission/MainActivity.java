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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.bluetooth.BluetoothHidDevice.*;
import static android.widget.AdapterView.*;
import static com.example.bt_transmission.BTindex.MY_UUID;
import static com.example.bt_transmission.BTindex.PSM;
import static com.example.bt_transmission.BTindex.Transmit_SPP;
import static com.example.bt_transmission.BTindex.Transmit_HID;
import static com.example.bt_transmission.BTindex.bluetoothAdapter;
import static com.example.bt_transmission.BTindex.bluetoothDevice;
import static com.example.bt_transmission.BTindex.bluetoothHidDevice;
import static com.example.bt_transmission.BTindex.bluetoothSocket;
import static com.example.bt_transmission.BTindex.targetMACaddress;
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
public class MainActivity extends AppCompatActivity  {
//Permission Check
//    if (ContextCompat.checkSelfPermission(MainActivity, Manifest.permission.BLUETOOTH)
//            != PackageManager.PERMISSION_GRANTED) {
    // Permission is not granted
//    }

    final MainActivity mainActivity = this;
    private void viewInfo(String str){
        Toast.makeText(mainActivity,str,Toast.LENGTH_SHORT).show();
    }

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
            } else {//show spinner
                Set<BluetoothDevice> pairedDevices = BTindex.bluetoothAdapter.getBondedDevices();
                final Spinner spinner = (Spinner) findViewById(R.id.Connection_Spinner);
                setSpinner(spinner,pairedDevices);
                spinner.setOnItemSelectedListener(
                        new OnItemSelectedListener(){
                            public void onItemSelected(AdapterView<?> parent, View view,int position, long id){
                                NameAddressPair pair = (NameAddressPair) spinner.getSelectedItem();
                                targetMACaddress = pair.getAddress();
                                viewInfo(pair.getAddress() +"is selected");
                            };
                            public void onNothingSelected(AdapterView<?> adapter){}
                        }
                );
            }

        }
        {
            CompoundButton button_Switch_SPP = findViewById(R.id.connect_switch1);
            CompoundButton button_Switch_HID = findViewById(R.id.connect_switch2);
            //SPP connection todo COMMENT OUTED
            /*
            button_Switch_SPP.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    try {
                        BTindex.bluetoothDevice = bluetoothAdapter.getRemoteDevice(targetMACaddress);
                        bluetoothSocket = BTindex.bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (bluetoothSocket != null){
                        if (isChecked){
                            try {
                                bluetoothSocket.connect();
                                Transmit_SPP = TRUE;
                                Toast.makeText(mainActivity,"Hello SPP",Toast.LENGTH_SHORT).show();
                                final TextView statusText = findViewById(R.id.status);
                                Runnable commands = new Runnable() {
                                    @Override
                                    public void run() {
                                        statusText.setText("SPP on " + LocalTime.now());
                                    }
                                };
                                ExecutorService executor = Executors.newFixedThreadPool(10);
                                executor.submit(commands);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }else{
                            try {
                                bluetoothSocket.close();
                                Transmit_SPP = FALSE;
                                bluetoothSocket = null;
                                Toast.makeText(mainActivity,"Good bye",Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
            */
            //HID connection
            button_Switch_HID.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
                BluetoothHidDeviceAppQosSettings qosSettings = new BluetoothHidDeviceAppQosSettings(BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT, 0, 0, 0, BluetoothHidDeviceAppQosSettings.MAX, BluetoothHidDeviceAppQosSettings.MAX);
                // all default
                Callback callback = new Callback() {
                    @Override
                    public void onGetReport(BluetoothDevice device, byte type, byte id, int bufferSize) {
                        super.onGetReport(device, type, id, bufferSize);
                    }
                };
                @Override
                public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                    try {
                        BTindex.bluetoothDevice = bluetoothAdapter.getRemoteDevice(targetMACaddress);
                        bluetoothSocket = BTindex.bluetoothDevice.createL2capChannel(PSM);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //ここからbluetoothHidDevice.getProfileProxyの設定のための変数
                    BluetoothProfile.ServiceListener listener = new BluetoothProfile.ServiceListener() {
                        //@Override
                        public void onServiceConnected(int profile, BluetoothProfile proxy) {
                            viewInfo("HERE");
                            if (profile == BluetoothProfile.HID_DEVICE){//note: check HID device (or INPUT_HOST)
                                viewInfo("Connected" + profile);
                                BTindex.bluetoothHidDevice = (BluetoothHidDevice) proxy;
                                BTindex.bluetoothHidDevice.registerApp(Sdp_Setting, qosSettings, qosSettings, executor, callback);
                                executor.execute(commands);
                            }
                        }

                        //@Override
                        public void onServiceDisconnected(int profile) {
                            Toast.makeText(mainActivity, "Good bye" + profile, Toast.LENGTH_SHORT).show();
                            if (profile == BluetoothHidDevice.HID_DEVICE){
                                bluetoothHidDevice = null;
                            }
                        }
                    };
                    //ここまでbluetoothHidDevice.getProfileProxyのための変数設定
                    bluetoothAdapter.getProfileProxy(MainActivity.this, listener, BluetoothProfile.HID_DEVICE);
                    //bluetoothService service = new bluetoothService();
                    if (isChecked) {

                            //todo bindServiceについて
                              // https://developer.android.com/reference/android/bluetooth/BluetoothHidDeviceAppSdpSettings
                              // https://developer.android.com/reference/android/bluetooth/BluetoothHidDevice#registerApp(android.bluetooth.BluetoothHidDeviceAppSdpSettings,%20android.bluetooth.BluetoothHidDeviceAppQosSettings,%20android.bluetooth.BluetoothHidDeviceAppQosSettings,%20java.util.concurrent.Executor,%20android.bluetooth.BluetoothHidDevice.Callback)


                        if (bluetoothHidDevice == null){
                            viewInfo("NULL");
                        }else{
                            BTindex.bluetoothHidDevice.registerApp(Sdp_Setting, qosSettings, qosSettings, executor, callback);
                            executor.execute(commands);
                            bluetoothHidDevice.connect(bluetoothDevice);
                            Transmit_HID = TRUE;
                        }
                        try {
                            //Intent intent = new Intent(mainActivity,bluetoothService.class);
                            //bindService(intent,service,Context.BIND_AUTO_CREATE);

                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                        // todo : BluetoothService Check
                    }
                }
            });
            //入力部門
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

                        if (Transmit_SPP){
                            try {
                                OutputStream btOutput = bluetoothSocket.getOutputStream();
                                String strTmp = "Hello,world!!!";
                                byte[] strByteArray = strTmp.getBytes();
                                btOutput.write(strByteArray);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (Transmit_HID){
                            bluetoothHidDevice.sendReport(bluetoothDevice,0,new byte[]{(byte) 0,(byte) 0,(byte) 127});
                        }//todo 何入れるか問題
                    }
                });
                //set Right-Click moving
                button_Click_R.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        strTmp.append("R");
                        textView3.setText(strTmp);

                        if (Transmit_SPP){
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
    }
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
    /*
    public class bluetoothService implements ServiceConnection{
        private Handler handler;
        public class MyServiceLocalBinder extends Binder {
            //&#x30b5;&#x30fc;&#x30d3;&#x30b9;&#x306e;&#x53d6;&#x5f97;
            bluetoothService getService() {
                return bluetoothService.this;
            }
        }

        public static final int SENDING = 0;
        public static final int PENDING = 1;
        private final IBinder mBinder = new MyServiceLocalBinder();

        public IBinder onBind(Intent intent){
            Log.i("LINE460","ON BIND");
            return mBinder;
        }
        public void onUnbind(Intent intent){
            Log.i("LINE473", "onUnbind" + ": " + intent);
        }
        //todo: Method for Clients
        public int getRandomNumber(){
            final Random random = new Random();
            return random.nextInt(100);
        }



        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.HID_DEVICE){//note:check HID device (or INPUT_HOST)
                Log.i("onConnected","Connected On HID");
                BTindex.bluetoothHidDevice = (BluetoothHidDevice) proxy;
            }
        }

        public void onServiceDisconnected(int profile) {
            Log.i("Bye","Disconnected On HID");
            if (profile == BluetoothHidDevice.HID_DEVICE){
                BTindex.bluetoothHidDevice = null;
            }
        }

        private class ConnectedThread extends Thread{
            private static final String TAG = "Transmitter";
            private BluetoothSocket mmSocket = null;
            private InputStream mmInStream = null;
            private OutputStream mmOutStream = null;
            private byte[] mmBuffer = null;

            public ConnectedThread(BluetoothSocket socket){
                mmSocket = socket;
                InputStream tmpIn = null;
                OutputStream tmpOut = null;

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


            // Call this from the main activity to send data to the remote device.
            public void write(byte[] bytes) {
                try {
                    mmOutStream.write(bytes);

                    // Share the sent message with the UI activity.
                    Message writtenMsg = handler.obtainMessage(
                            1, -1, -1, mmBuffer);//MessageConstants.MESSAGE_WRITE = 1
                    writtenMsg.sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "Error occurred when sending data", e);

                    // Send a failure message back to the activity.
                    Message writeErrorMsg =
                            handler.obtainMessage(2);//MessageConstants.MESSAGE_TOAST=2
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
}


