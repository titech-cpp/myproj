//package com.example.bt_sendmodule;
//Initial imports

package com.example.sakana.bluetoothcom;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.bt_sendmodule.R;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBTAdapter = null;
    private BluetoothDevice mBTDevice = null;
    private BluetoothSocket mBTSocket = null;
    private OutputStream mOutputStream = null;//出力ストリーム

    private Button btnSend;//送信用ボタン
    private Button btnFinish;//終了用ボタン
    private TextView textview;//MacAddress表示用
    private String MacAddress = "3c:91:80:5c:1b:7c";
    private String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSend = (Button)findViewById(R.id.btnSend);
        btnFinish = (Button)findViewById(R.id.btnFinish);
        textview = (TextView)findViewById(R.id.textView);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBTSocket != null) {
                    Send();
                }
            }
        });
        btnFinish.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //ソケットを確立する関数
        BTConnect();

        //ソケットが取得出来たら、出力用ストリームを作成する
        if(mBTSocket != null){
            try{
                mOutputStream = mBTSocket.getOutputStream();
            }catch(IOException e){/*ignore*/}
        }else{
            btnSend.setText("mBTSocket == null !!");
        }


    }

    private void BTConnect(){
        //BTアダプタのインスタンスを取得
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();

        textview.setText(MacAddress);
        //相手先BTデバイスのインスタンスを取得
        mBTDevice = mBTAdapter.getRemoteDevice(MacAddress);
        //ソケットの設定
        try {
            mBTSocket = mBTDevice.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
        } catch (IOException e) {
            mBTSocket = null;
        }

        if(mBTSocket != null) {
            //接続開始
            mBTAdapter.cancelDiscovery();
            try {
                mBTSocket.connect();
            } catch (IOException connectException) {
                try {
                    mBTSocket.close();
                    mBTSocket = null;
                } catch (IOException closeException) {
                    return;
                }
            }
        }
    }

    private void Send(){
        //文字列を送信する
        byte[] bytes = {};
        String str = "Hello World!";
        bytes = str.getBytes();
        try {
            //ここで送信
            mOutputStream.write(bytes);
        } catch (IOException e) {
            try{
                mBTSocket.close();
            }catch(IOException e1){/*ignore*/}
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(mBTSocket != null){
            try {
                mBTSocket.connect();
            } catch (IOException connectException) {/*ignore*/}
            mBTSocket = null;
        }
    }
}