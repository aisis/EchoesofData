package com.starkey.bledevice;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;

public class KitKatBLEScanner implements BLEScanner {
    private static String TAG;
    private BLEScanCallBack mClientScanCallback;
    private LeScanCallback mScanCallback;

    static {
        TAG = KitKatBLEScanner.class.getSimpleName();
    }

    public KitKatBLEScanner() {
        this.mScanCallback = new 1(this);
    }

    public void startSearchingForDevices(String str, BLEScanCallBack bLEScanCallBack) {
        this.mClientScanCallback = bLEScanCallBack;
        BluetoothAdapter.getDefaultAdapter().startLeScan(this.mScanCallback);
    }

    public void stopSearchingForDevices() {
        BluetoothAdapter.getDefaultAdapter().stopLeScan(this.mScanCallback);
        if (this.mClientScanCallback != null) {
            this.mClientScanCallback.scanTimeComplete();
        }
    }
}
