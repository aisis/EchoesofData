package com.starkey.bledevice;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BLEScanService extends Service {
    public static final String ACTION_NEW_DEVICE_FOUND = "com.starkey.bledevice.BLEScanService.ACTION_NEW_DEVICE_FOUND";
    public static final String ACTION_SCAN_COMPLETE = "com.starkey.bledevice.BLEScanService.ACTION_SCAN_COMPLETE";
    public static final String EXTRA_ADDRESS = "com.starkey.bledevice.BLEScanService.EXTRA_ADDRESS";
    public static final String EXTRA_BONDED = "com.starkey.bledevice.BLEScanService.EXTRA_BONDED";
    public static final String EXTRA_NAME = "com.starkey.bledevice.BLEScanService.EXTRA_NAME";
    protected static final long SCAN_PERIOD = 10000;
    private String TAG;
    private boolean mAddBondedDevices;
    private final IBinder mBinder;
    protected BLEScanCallBack mClientScanCallback;
    private List<BluetoothDevice> mDevicesFound;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private BLEScanner mScanner;

    public class LocalBinder extends Binder {
        public BLEScanService getService() {
            return BLEScanService.this;
        }
    }

    public BLEScanService() {
        this.TAG = BLEScanService.class.getSimpleName();
        this.mBinder = new LocalBinder();
    }

    private void CleanUpHandler() {
        this.mHandlerThread.quitSafely();
    }

    private Handler CreateHandler(String str) {
        this.mHandlerThread = new HandlerThread(str);
        this.mHandlerThread.start();
        return new Handler(this.mHandlerThread.getLooper());
    }

    private void broadCastNewDevice(BluetoothDevice bluetoothDevice) {
        Log.d(this.TAG, "broadCastNewDevice:  " + bluetoothDevice.getAddress());
        Intent intent = new Intent(ACTION_NEW_DEVICE_FOUND);
        intent.putExtra(EXTRA_ADDRESS, bluetoothDevice.getAddress());
        intent.putExtra(EXTRA_NAME, bluetoothDevice.getName());
        if (bluetoothDevice.getBondState() == 12) {
            Log.i(this.TAG, "broadCastNewDevice: device is bonded");
            intent.putExtra(EXTRA_BONDED, true);
        } else {
            intent.putExtra(EXTRA_BONDED, false);
        }
        sendBroadcast(intent);
    }

    private void broadCastScanDone() {
        sendBroadcast(new Intent(ACTION_SCAN_COMPLETE));
    }

    public Set<BluetoothDevice> getBondedDevices() {
        return BluetoothAdapter.getDefaultAdapter().getBondedDevices();
    }

    public void getStarkeyAids() {
        Log.d(this.TAG, "getStarkeyAids: ");
        startSearchingForDevices(SHIPGattAttributes.SHIP_SERVICE);
    }

    public void getStarkeyAidsAndBondedDevices() {
        this.mAddBondedDevices = true;
        getStarkeyAids();
    }

    public IBinder onBind(Intent intent) {
        Log.d(this.TAG, "onBind: ");
        return this.mBinder;
    }

    public boolean onUnbind(Intent intent) {
        Log.d(this.TAG, "onUnbind: ");
        return super.onUnbind(intent);
    }

    public void startSearchingForDevices(String str) {
        Log.d(this.TAG, "startSearchingForDevices: " + str);
        if (VERSION.SDK_INT > 19) {
            this.mScanner = new LolliBLEScanner();
        } else {
            this.mScanner = new KitKatBLEScanner();
        }
        this.mDevicesFound = new ArrayList();
        this.mClientScanCallback = new BLEScanCallBack() {
            public void newDeviceFound(BluetoothDevice bluetoothDevice) {
                if (!BLEScanService.this.mDevicesFound.contains(bluetoothDevice)) {
                    BLEScanService.this.broadCastNewDevice(bluetoothDevice);
                    BLEScanService.this.mDevicesFound.add(bluetoothDevice);
                }
            }

            public void scanTimeComplete() {
                if (BLEScanService.this.mAddBondedDevices) {
                    BLEScanService.this.mAddBondedDevices = false;
                    for (BluetoothDevice bluetoothDevice : BLEScanService.this.getBondedDevices()) {
                        if (!BLEScanService.this.mDevicesFound.contains(bluetoothDevice)) {
                            BLEScanService.this.broadCastNewDevice(bluetoothDevice);
                            BLEScanService.this.mDevicesFound.add(bluetoothDevice);
                        }
                    }
                }
                BLEScanService.this.broadCastScanDone();
            }
        };
        this.mHandler = CreateHandler("ScanTimer");
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                BLEScanService.this.stopSearchingForDevices();
            }
        }, SCAN_PERIOD);
        this.mScanner.startSearchingForDevices(str, this.mClientScanCallback);
    }

    public void stopSearchingForDevices() {
        Log.d(this.TAG, "stopSearchingForDevices: ");
        CleanUpHandler();
        if (this.mScanner != null) {
            this.mScanner.stopSearchingForDevices();
        }
    }
}
