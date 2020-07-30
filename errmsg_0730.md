07/30 17:30:39: Launching 'app' on realme RMX1931.
$ adb shell am start -n "com.example.bt_transmission/com.example.bt_transmission.MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
Connected to process 21529 on device 'realme-rmx1931-16349004'.
Capturing and displaying logcat messages from application. This behavior can be disabled in the "Logcat output" section of the "Debugger" settings page.
E/libc: Access denied finding property "persist.sys.theme"
I/Perf: Connecting to perf service.
E/Perf: Fail to get file list com.example.bt_transmission
E/Perf: getFolderSize() : Exception_1 = java.lang.NullPointerException: Attempt to get length of null array
I/bt_transmissio: ProcessProfilingInfo new_methods=0 is saved saved_to_disk=0 resolve_classes_delay=5000
W/ActivityThread: handleWindowVisibility: no activity for token android.os.BinderProxy@c178cee
W/bt_transmissio: Accessing hidden method Landroid/view/View;->computeFitSystemWindows(Landroid/graphics/Rect;Landroid/graphics/Rect;)Z (greylist, reflection, allowed)
    Accessing hidden method Landroid/view/ViewGroup;->makeOptionalFitsSystemWindows()V (greylist, reflection, allowed)
D/TestOverScroll: getScaledOverscrollDistance: b
E/CheckPermission: _bluetooth code = 1 
D/WindowManager: Add to mViews: DecorView@684e8a[MainActivity], this = android.view.WindowManagerGlobal@d13acfb,pkg= com.example.bt_transmission
I/AdrenoGLES: QUALCOMM build                   : fcc7d63, I6b4f1e501f
    Build Date                       : 11/02/19
    OpenGL ES Shader Compiler Version: EV031.27.05.02
    Local Branch                     : 
    Remote Branch                    : quic/gfx-adreno.lnx.1.0.r74-rel
    Remote Branch                    : NONE
    Reconstruct Branch               : NOTHING
    Build Config                     : S P 8.0.12 AArch64
I/AdrenoGLES: PFP: 0x016ee187, ME: 0x00000000
W/AdrenoUtils: <ReadGpuID_from_sysfs:194>: Failed to open /sys/class/kgsl/kgsl-3d0/gpu_model
    <ReadGpuID:218>: Failed to read chip ID from gpu_model. Fallback to use the GSL path
W/Gralloc3: mapper 3.x is not supported
I/Choreographer: Skipped 5 frames!  The application may be doing too much work on its main thread.
D/WindowManager: Add to mViews: com.color.internal.widget.ColorToastLayout{1719dfe V.E...... ......I. 0,0-0,0 #c0204f9 oppo:id/color_toast_layout}, this = android.view.WindowManagerGlobal@d13acfb,pkg= com.example.bt_transmission
D/ColorViewRootUtil: nav bar mode ignore false downX 990 downY 436 mScreenHeight 2400 mScreenWidth 1080 mStatusBarHeight 54 globalScale 1.125 nav mode 0 rotation 0 event MotionEvent { action=ACTION_DOWN, actionButton=0, id[0]=0, x[0]=990.0, y[0]=436.0, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, classification=NONE, metaState=0, flags=0x2, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=458445961, downTime=458445961, deviceId=3, source=0x1002, displayId=0 }
E/CheckPermission: _bluetooth code = 3 
D/BluetoothHidDevice: Binding service...
E/BluetoothHidDevice: Could not bind to Bluetooth Service with Intent { act=android.bluetooth.IBluetoothHidDevice }
D/BluetoothSocket: close() this: android.bluetooth.BluetoothSocket@eaac7b, channel: 11, mSocketIS: null, mSocketOS: nullmSocket: null, mSocketState: INIT
I/Choreographer: Skipped 2 frames!  The application may be doing too much work on its main thread.
D/WindowManager: Add to mViews: com.color.internal.widget.ColorToastLayout{1b2ad2d V.E...... ......I. 0,0-0,0 #c0204f9 oppo:id/color_toast_layout}, this = android.view.WindowManagerGlobal@d13acfb,pkg= com.example.bt_transmission