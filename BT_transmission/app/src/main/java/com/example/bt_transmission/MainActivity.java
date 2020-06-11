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

                    //ここからbluetoothHidDeviceの設定のための変数
                    BluetoothProfile.ServiceListener listener = new BluetoothProfile.ServiceListener() {
                        @Override
                        public void onServiceConnected(int profile, BluetoothProfile proxy) {
                            System.out.print("Connected On ServiceListener");
                        }

                        @Override
                        public void onServiceDisconnected(int profile) {
                            System.out.print("Disconnected On ServiceListener");
                        }
                    };
                    Context Discript_context = new Context() {
                        @Override
                        public AssetManager getAssets() {
                            return null;
                        }

                        @Override
                        public Resources getResources() {
                            return null;
                        }

                        @Override
                        public PackageManager getPackageManager() {
                            return null;
                        }

                        @Override
                        public ContentResolver getContentResolver() {
                            return null;
                        }

                        @Override
                        public Looper getMainLooper() {
                            return null;
                        }

                        @Override
                        public Context getApplicationContext() {
                            return null;
                        }

                        @Override
                        public void setTheme(int resid) {

                        }

                        @Override
                        public Resources.Theme getTheme() {
                            return null;
                        }

                        @Override
                        public ClassLoader getClassLoader() {
                            return null;
                        }

                        @Override
                        public String getPackageName() {
                            return null;
                        }

                        @Override
                        public ApplicationInfo getApplicationInfo() {
                            return null;
                        }

                        @Override
                        public String getPackageResourcePath() {
                            return null;
                        }

                        @Override
                        public String getPackageCodePath() {
                            return null;
                        }

                        @Override
                        public SharedPreferences getSharedPreferences(String name, int mode) {
                            return null;
                        }

                        @Override
                        public boolean moveSharedPreferencesFrom(Context sourceContext, String name) {
                            return false;
                        }

                        @Override
                        public boolean deleteSharedPreferences(String name) {
                            return false;
                        }

                        @Override
                        public FileInputStream openFileInput(String name) throws FileNotFoundException {
                            return null;
                        }

                        @Override
                        public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
                            return null;
                        }

                        @Override
                        public boolean deleteFile(String name) {
                            return false;
                        }

                        @Override
                        public File getFileStreamPath(String name) {
                            return null;
                        }

                        @Override
                        public File getDataDir() {
                            return null;
                        }

                        @Override
                        public File getFilesDir() {
                            return null;
                        }

                        @Override
                        public File getNoBackupFilesDir() {
                            return null;
                        }

                        @Nullable
                        @Override
                        public File getExternalFilesDir(@Nullable String type) {
                            return null;
                        }

                        @Override
                        public File[] getExternalFilesDirs(String type) {
                            return new File[0];
                        }

                        @Override
                        public File getObbDir() {
                            return null;
                        }

                        @Override
                        public File[] getObbDirs() {
                            return new File[0];
                        }

                        @Override
                        public File getCacheDir() {
                            return null;
                        }

                        @Override
                        public File getCodeCacheDir() {
                            return null;
                        }

                        @Nullable
                        @Override
                        public File getExternalCacheDir() {
                            return null;
                        }

                        @Override
                        public File[] getExternalCacheDirs() {
                            return new File[0];
                        }

                        @Override
                        public File[] getExternalMediaDirs() {
                            return new File[0];
                        }

                        @Override
                        public String[] fileList() {
                            return new String[0];
                        }

                        @Override
                        public File getDir(String name, int mode) {
                            return null;
                        }

                        @Override
                        public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
                            return null;
                        }

                        @Override
                        public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, @Nullable DatabaseErrorHandler errorHandler) {
                            return null;
                        }

                        @Override
                        public boolean moveDatabaseFrom(Context sourceContext, String name) {
                            return false;
                        }

                        @Override
                        public boolean deleteDatabase(String name) {
                            return false;
                        }

                        @Override
                        public File getDatabasePath(String name) {
                            return null;
                        }

                        @Override
                        public String[] databaseList() {
                            return new String[0];
                        }

                        @Override
                        public Drawable getWallpaper() {
                            return null;
                        }

                        @Override
                        public Drawable peekWallpaper() {
                            return null;
                        }

                        @Override
                        public int getWallpaperDesiredMinimumWidth() {
                            return 0;
                        }

                        @Override
                        public int getWallpaperDesiredMinimumHeight() {
                            return 0;
                        }

                        @Override
                        public void setWallpaper(Bitmap bitmap) throws IOException {

                        }

                        @Override
                        public void setWallpaper(InputStream data) throws IOException {

                        }

                        @Override
                        public void clearWallpaper() throws IOException {

                        }

                        @Override
                        public void startActivity(Intent intent) {

                        }

                        @Override
                        public void startActivity(Intent intent, @Nullable Bundle options) {

                        }

                        @Override
                        public void startActivities(Intent[] intents) {

                        }

                        @Override
                        public void startActivities(Intent[] intents, Bundle options) {

                        }

                        @Override
                        public void startIntentSender(IntentSender intent, @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws IntentSender.SendIntentException {

                        }

                        @Override
                        public void startIntentSender(IntentSender intent, @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, @Nullable Bundle options) throws IntentSender.SendIntentException {

                        }

                        @Override
                        public void sendBroadcast(Intent intent) {

                        }

                        @Override
                        public void sendBroadcast(Intent intent, @Nullable String receiverPermission) {

                        }

                        @Override
                        public void sendOrderedBroadcast(Intent intent, @Nullable String receiverPermission) {

                        }

                        @Override
                        public void sendOrderedBroadcast(@NonNull Intent intent, @Nullable String receiverPermission, @Nullable BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {

                        }

                        @Override
                        public void sendBroadcastAsUser(Intent intent, UserHandle user) {

                        }

                        @Override
                        public void sendBroadcastAsUser(Intent intent, UserHandle user, @Nullable String receiverPermission) {

                        }

                        @Override
                        public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, @Nullable String receiverPermission, BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {

                        }

                        @Override
                        public void sendStickyBroadcast(Intent intent) {

                        }

                        @Override
                        public void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {

                        }

                        @Override
                        public void removeStickyBroadcast(Intent intent) {

                        }

                        @Override
                        public void sendStickyBroadcastAsUser(Intent intent, UserHandle user) {

                        }

                        @Override
                        public void sendStickyOrderedBroadcastAsUser(Intent intent, UserHandle user, BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {

                        }

                        @Override
                        public void removeStickyBroadcastAsUser(Intent intent, UserHandle user) {

                        }

                        @Nullable
                        @Override
                        public Intent registerReceiver(@Nullable BroadcastReceiver receiver, IntentFilter filter) {
                            return null;
                        }

                        @Nullable
                        @Override
                        public Intent registerReceiver(@Nullable BroadcastReceiver receiver, IntentFilter filter, int flags) {
                            return null;
                        }

                        @Nullable
                        @Override
                        public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, @Nullable String broadcastPermission, @Nullable Handler scheduler) {
                            return null;
                        }

                        @Nullable
                        @Override
                        public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, @Nullable String broadcastPermission, @Nullable Handler scheduler, int flags) {
                            return null;
                        }

                        @Override
                        public void unregisterReceiver(BroadcastReceiver receiver) {

                        }

                        @Nullable
                        @Override
                        public ComponentName startService(Intent service) {
                            return null;
                        }

                        @Nullable
                        @Override
                        public ComponentName startForegroundService(Intent service) {
                            return null;
                        }

                        @Override
                        public boolean stopService(Intent service) {
                            return false;
                        }

                        @Override
                        public boolean bindService(Intent service, @NonNull ServiceConnection conn, int flags) {
                            return false;
                        }

                        @Override
                        public void unbindService(@NonNull ServiceConnection conn) {

                        }

                        @Override
                        public boolean startInstrumentation(@NonNull ComponentName className, @Nullable String profileFile, @Nullable Bundle arguments) {
                            return false;
                        }

                        @Override
                        public Object getSystemService(@NonNull String name) {
                            return null;
                        }

                        @Nullable
                        @Override
                        public String getSystemServiceName(@NonNull Class<?> serviceClass) {
                            return null;
                        }

                        @SuppressLint("WrongConstant")
                        @Override
                        public int checkPermission(@NonNull String permission, int pid, int uid) {
                            return 0;
                        }

                        @SuppressLint("WrongConstant")
                        @Override
                        public int checkCallingPermission(@NonNull String permission) {
                            return 0;
                        }

                        @SuppressLint("WrongConstant")
                        @Override
                        public int checkCallingOrSelfPermission(@NonNull String permission) {
                            return 0;
                        }

                        @SuppressLint("WrongConstant")
                        @Override
                        public int checkSelfPermission(@NonNull String permission) {
                            return 0;
                        }

                        @Override
                        public void enforcePermission(@NonNull String permission, int pid, int uid, @Nullable String message) {

                        }

                        @Override
                        public void enforceCallingPermission(@NonNull String permission, @Nullable String message) {

                        }

                        @Override
                        public void enforceCallingOrSelfPermission(@NonNull String permission, @Nullable String message) {

                        }

                        @Override
                        public void grantUriPermission(String toPackage, Uri uri, int modeFlags) {

                        }

                        @Override
                        public void revokeUriPermission(Uri uri, int modeFlags) {

                        }

                        @Override
                        public void revokeUriPermission(String toPackage, Uri uri, int modeFlags) {

                        }

                        @SuppressLint("WrongConstant")
                        @Override
                        public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
                            return 0;
                        }

                        @SuppressLint("WrongConstant")
                        @Override
                        public int checkCallingUriPermission(Uri uri, int modeFlags) {
                            return 0;
                        }

                        @SuppressLint("WrongConstant")
                        @Override
                        public int checkCallingOrSelfUriPermission(Uri uri, int modeFlags) {
                            return 0;
                        }

                        @SuppressLint("WrongConstant")
                        @Override
                        public int checkUriPermission(@Nullable Uri uri, @Nullable String readPermission, @Nullable String writePermission, int pid, int uid, int modeFlags) {
                            return 0;
                        }

                        @Override
                        public void enforceUriPermission(Uri uri, int pid, int uid, int modeFlags, String message) {

                        }

                        @Override
                        public void enforceCallingUriPermission(Uri uri, int modeFlags, String message) {

                        }

                        @Override
                        public void enforceCallingOrSelfUriPermission(Uri uri, int modeFlags, String message) {

                        }

                        @Override
                        public void enforceUriPermission(@Nullable Uri uri, @Nullable String readPermission, @Nullable String writePermission, int pid, int uid, int modeFlags, @Nullable String message) {

                        }

                        @Override
                        public Context createPackageContext(String packageName, int flags) throws PackageManager.NameNotFoundException {
                            return null;
                        }

                        @Override
                        public Context createContextForSplit(String splitName) throws PackageManager.NameNotFoundException {
                            return null;
                        }

                        @Override
                        public Context createConfigurationContext(@NonNull Configuration overrideConfiguration) {
                            return null;
                        }

                        @Override
                        public Context createDisplayContext(@NonNull Display display) {
                            return null;
                        }

                        @Override
                        public Context createDeviceProtectedStorageContext() {
                            return null;
                        }

                        @Override
                        public boolean isDeviceProtectedStorage() {
                            return false;
                        }
                    };
                    Callback callback = new Callback() {
                        @Override
                        public void onGetReport(BluetoothDevice device, byte type, byte id, int bufferSize) {
                            super.onGetReport(device, type, id, bufferSize);
                        }
                    };
                    //ここまでbluetoothHidDeviceのための変数設定
                    Boolean bluetoothAdapterProfileProxy = bluetoothAdapter.getProfileProxy(Discript_context, listener, BluetoothProfile.HID_DEVICE);
                    if (isChecked) {
                            byte BTMouse_class = SUBCLASS1_MOUSE;
                            BluetoothHidDeviceAppSdpSettings Sdp_Setting = new BluetoothHidDeviceAppSdpSettings("Real_BTMouse", "Virtual mouse on Real", "Programmer_Fish", BTMouse_class, BTindex.discriptor);
                            //この上の行のパラメータたぶん違う todo 特にSdp_settingの値
                            Executor executor = new Executor() {
                                @Override
                                public void execute(Runnable command) {

                                    //todo ここ何入れる？
                                }
                            };
                            // todo https://developer.android.com/reference/android/bluetooth/BluetoothHidDeviceAppSdpSettings
                            // todo https://developer.android.com/reference/android/bluetooth/BluetoothHidDevice#registerApp(android.bluetooth.BluetoothHidDeviceAppSdpSettings,%20android.bluetooth.BluetoothHidDeviceAppQosSettings,%20android.bluetooth.BluetoothHidDeviceAppQosSettings,%20java.util.concurrent.Executor,%20android.bluetooth.BluetoothHidDevice.Callback)
                            //todo 下の行の修正から

                            if (bluetoothAdapterProfileProxy) {
                                BTindex.bluetoothHidDevice.registerApp(Sdp_Setting, null, null, executor, callback);

                                try {
                                    bluetoothSocket.connect();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

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