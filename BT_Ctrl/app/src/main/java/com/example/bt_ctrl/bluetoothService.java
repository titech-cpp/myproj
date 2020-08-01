package com.example.bt_ctrl;

import android.app.Service;
import android.bluetooth.BluetoothHidDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.io.IOException;
import java.util.Random;

import static com.example.bt_ctrl.BTindex.Transmit_HID;
import static com.example.bt_ctrl.BTindex.bluetoothAdapter;
import static com.example.bt_ctrl.BTindex.bluetoothHidDevice;
import static com.example.bt_ctrl.BTindex.bluetoothSocket;
import static java.lang.Boolean.FALSE;

public class bluetoothService extends Service {
    // service binder ////////////////
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public bluetoothService getService() {
            return bluetoothService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        bluetoothAdapter.closeProfileProxy(BluetoothHidDevice.HID_DEVICE,bluetoothHidDevice);
        try {
            bluetoothSocket.close();
            Transmit_HID = FALSE;
            bluetoothSocket = null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return super.onUnbind(intent);
    }
    /*Methods*/
    public int getNum(){
        Random random = new Random();
        return random.nextInt(10);
    }
}
