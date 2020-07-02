E/BluetoothHidDevice: Could not bind to Bluetooth Service with Intent { act=android.bluetooth.IBluetoothHidDevice }
W/BluetoothAdapter: getBluetoothService() called with no BluetoothManagerCallback
W/System.err: java.io.IOException: read failed, socket might closed or timeout, read ret: -1
W/System.err:     at android.bluetooth.BluetoothSocket.readAll(BluetoothSocket.java:772)
W/System.err:     at android.bluetooth.BluetoothSocket.readInt(BluetoothSocket.java:786)
        at android.bluetooth.BluetoothSocket.connect(BluetoothSocket.java:404)
        at com.example.bt_transmission.MainActivity$3.onCheckedChanged(MainActivity.java:248)
W/System.err:     at android.widget.CompoundButton.setChecked(CompoundButton.java:180)
W/System.err:     at android.widget.Switch.setChecked(Switch.java:1167)
        at android.widget.Switch.toggle(Switch.java:1162)
        at android.widget.CompoundButton.performClick(CompoundButton.java:140)
W/System.err:     at android.view.View.performClickInternal(View.java:7221)
        at android.view.View.access$3800(View.java:821)
W/System.err:     at android.view.View$PerformClick.run(View.java:27716)
W/System.err:     at android.os.Handler.handleCallback(Handler.java:883)
        at android.os.Handler.dispatchMessage(Handler.java:100)
W/System.err:     at android.os.Looper.loop(Looper.java:227)
        at android.app.ActivityThread.main(ActivityThread.java:7835)
        at java.lang.reflect.Method.invoke(Native Method)
        at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:492)
W/System.err:     at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:980)
E/ANR_LOG: >>> msg's executing time is too long
    Blocked msg = { when=-30s49ms what=0 target=android.view.ViewRootImpl$ViewRootHandler callback=android.view.View$PerformClick } , cost  = 30049 ms
    >>>Current msg List is:
    Current msg <1>  = { when=-30s49ms what=0 target=android.view.ViewRootImpl$ViewRootHandler callback=android.view.View$UnsetPressedState }
    Current msg <2>  = { when=-30s48ms barrier=7 }
    Current msg <3>  = { when=-30s35ms what=0 target=android.os.Handler callback=android.view.-$$Lambda$1kvF4JuyM42-wmyDVPAIYdPz1jE }
E/ANR_LOG: Current msg <4>  = { when=-30s22ms what=0 target=android.widget.Toast$TN$1 obj=android.os.BinderProxy@2c75b2a }
    Current msg <5>  = { when=-29s892ms what=0 target=android.os.Handler callback=android.view.-$$Lambda$1kvF4JuyM42-wmyDVPAIYdPz1jE }
    Current msg <6>  = { when=-29s891ms what=0 target=android.os.Handler callback=android.view.-$$Lambda$1kvF4JuyM42-wmyDVPAIYdPz1jE }
    Current msg <7>  = { when=-29s891ms what=0 target=android.os.Handler callback=android.view.-$$Lambda$1kvF4JuyM42-wmyDVPAIYdPz1jE }
    Current msg <8>  = { when=-28s22ms what=1 target=android.widget.Toast$TN$1 }
    >>>CURRENT MSG DUMP OVER<<<