package com.starkey.bledevice;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.bluetooth.le.ScanSettings.Builder;
import android.os.ParcelUuid;
import java.util.ArrayList;
import java.util.List;

@TargetApi(21)
public class LolliBLEScanner implements BLEScanner {
    private static String TAG;
    private static final ScanSettings mScanSettings;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BLEScanCallBack mClientScanCallback;
    protected ScanCallback mScanCallback;

    static {
        TAG = LolliBLEScanner.class.getSimpleName();
        mScanSettings = new Builder().setScanMode(1).setCallbackType(1).build();
    }

    public LolliBLEScanner() {
        this.mScanCallback = new 1(this);
        this.mBluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
    }

    public void startSearchingForDevices(String str, BLEScanCallBack bLEScanCallBack) {
        this.mClientScanCallback = bLEScanCallBack;
        List arrayList = new ArrayList();
        arrayList.add(new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(str)).build());
        this.mBluetoothLeScanner.startScan(arrayList, mScanSettings, this.mScanCallback);
    }

    public void stopSearchingForDevices() {
        this.mBluetoothLeScanner.stopScan(this.mScanCallback);
        if (this.mClientScanCallback != null) {
            this.mClientScanCallback.scanTimeComplete();
        }
    }
}

