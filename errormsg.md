2020-06-22 16:57:52.532 17411-17411/com.example.bt_transmission E/AndroidRuntime: FATAL EXCEPTION: main
    Process: com.example.bt_transmission, PID: 17411
    java.lang.NullPointerException: Attempt to invoke virtual method 'boolean android.bluetooth.BluetoothHidDevice.registerApp(android.bluetooth.BluetoothHidDeviceAppSdpSettings, android.bluetooth.BluetoothHidDeviceAppQosSettings, android.bluetooth.BluetoothHidDeviceAppQosSettings, java.util.concurrent.Executor, android.bluetooth.BluetoothHidDevice$Callback)' on a null object reference
        at com.example.bt_transmission.MainActivity$3.onCheckedChanged(MainActivity.java:300)
        at android.widget.CompoundButton.setChecked(CompoundButton.java:180)
        at android.widget.Switch.setChecked(Switch.java:1167)
        at android.widget.Switch.toggle(Switch.java:1162)
        at android.widget.CompoundButton.performClick(CompoundButton.java:140)
        at android.view.View.performClickInternal(View.java:7221)
        at android.view.View.access$3800(View.java:821)
        at android.view.View$PerformClick.run(View.java:27716)
        at android.os.Handler.handleCallback(Handler.java:883)
        at android.os.Handler.dispatchMessage(Handler.java:100)
        at android.os.Looper.loop(Looper.java:227)
        at android.app.ActivityThread.main(ActivityThread.java:7835)
        at java.lang.reflect.Method.invoke(Native Method)
        at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:492)
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:980)
2020-06-22 16:57:52.554 17411-17411/com.example.bt_transmission I/Process: Sending signal. PID: 17411 SIG: 9
