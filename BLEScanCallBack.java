package com.starkey.bledevice;

import android.bluetooth.BluetoothDevice;

public interface BLEScanCallBack {
    void newDeviceFound(BluetoothDevice bluetoothDevice);

    void scanTimeComplete();
}
