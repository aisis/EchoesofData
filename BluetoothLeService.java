package com.starkey.bledevice;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Binder;
import android.os.Build.VERSION;
import android.os.IBinder;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.altbeacon.beacon.BeaconManager;

public class BluetoothLeService extends Service {
    public static final String ACTION_CHARACTERISTIC_WROTE = "com.starkey.bledevice.BluetoothLeService.ACTION_CHARACTERISTIC_WROTE";
    public static final String ACTION_CHAR_UPDATED = "com.starkey.bledevice.BluetoothLeService.ACTION_CHAR_UPDATED";
    public static final String ACTION_DATA_AVAILABLE = "com.starkey.bledevice.BluetoothLeService.ACTION_DATA_AVAILABLE";
    public static final String ACTION_DESCRIPTOR_WROTE = "com.starkey.bledevice.BluetoothLeService.ACTION_DESCRIPTOR_WROTE";
    public static final String ACTION_GATT_CONNECTED = "com.starkey.bledevice.BluetoothLeService.ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_CONNECT_FAILED = "com.starkey.bledevice.BluetoothLeService.ACTION_ACTION_GATT_CONNECT_FAILED";
    public static final String ACTION_GATT_DISCONNECTED = "com.starkey.bledevice.BluetoothLeService.ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_READ_ERROR = "com.starkey.bledevice.BluetoothLeService.ACTION_ACTION_GATT_READ_ERROR";
    public static final String ACTION_GATT_SERVICES_DISCOVERED = "com.starkey.bledevice.BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED";
    public static final String ACTION_GATT_SET_NOTIFY_ERROR = "com.starkey.bledevice.BluetoothLeService.ACTION_ACTION_GATT_SET_NOTIFY_ERROR";
    public static final String ACTION_GATT_SSI_ERROR = "com.starkey.bledevice.BluetoothLeService.ACTION_GATT_SSI_ERROR";
    public static final String ACTION_GATT_WRITE_ERROR = "com.starkey.bledevice.BluetoothLeService.ACTION_ACTION_GATT_WRITE_ERROR";
    public static final String ACTION_READ_REMOTE_RSSI = "com.starkey.bledevice.BluetoothLeService.ACTION_READ_REMOTE_RSSI";
    public static final String ACTION_SSI_DATA_AVAILABLE = "com.starkey.bledevice.BluetoothLeService.ACTION_SSI_DATA_AVAILABLE";
    public static final String ACTION_STATUS5_RECEIVED = "com.starkey.bledevice.BluetoothLeService.ACTION_STATUS5_RECEIVED";
    public static final String ADDRESS = "com.starkey.bledevice.BluetoothLeService.ADDRESS";
    public static final int DEFAULT_SPEED = 0;
    public static final String EXTRA_BYTE_DATA = "com.starkey.bledevice.BluetoothLeService.EXTRA_BYTE_DATA";
    public static final String EXTRA_RSSI = "com.starkey.bledevice.BluetoothLeService.EXTRA_RSSI";
    public static final String EXTRA_STRING_CHARACTERISTIC = "com.starkey.bledevice.BluetoothLeService.EXTRA_STRING_CHARACTERISTIC";
    public static final String EXTRA_STRING_DATA = "com.starkey.bledevice.BluetoothLeService.EXTRA_STRING_DATA";
    private static final int STATE_CONNECTED = 2;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_DISCONNECTED = 0;
    public static final int SUPER_FAST = 1;
    public static final int SUPER_SLOW = 2;
    private static final String TAG;
    private static boolean mIsBusy;
    private static final Queue<ReadWrite> mReadWriteQueue;
    private final IBinder mBinder;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private HashMap<String, Integer> mConnectionState;
    private HashMap<String, BluetoothGatt> mConnections;
    private HashMap<String, Boolean> mDisconnectCalled;
    private SSIResponseHandler mSSIResponseHandler;
    private Timer mTimer;

    /* renamed from: com.starkey.bledevice.BluetoothLeService.10 */
    class AnonymousClass10 extends ReadWrite {
        final /* synthetic */ BluetoothGattCharacteristic val$SSILast;
        final /* synthetic */ String val$address;
        final /* synthetic */ byte[] val$transferBuffer;
        final /* synthetic */ boolean val$transmitOnly;

        AnonymousClass10(String str, String str2, BluetoothGattCharacteristic bluetoothGattCharacteristic, byte[] bArr, boolean z) {
            this.val$address = str2;
            this.val$SSILast = bluetoothGattCharacteristic;
            this.val$transferBuffer = bArr;
            this.val$transmitOnly = z;
            super(str);
        }

        protected void ioAction() {
            BluetoothLeService.this.writeCharacteristic(this.val$address, this.val$SSILast, this.val$transferBuffer);
            if (this.val$transmitOnly) {
                BluetoothLeService.mIsBusy = false;
                BluetoothLeService.this.nextRequest();
            }
        }
    }

    /* renamed from: com.starkey.bledevice.BluetoothLeService.1 */
    class AnonymousClass1 extends ReadWrite {
        final /* synthetic */ String val$address;

        AnonymousClass1(String str, String str2) {
            this.val$address = str2;
            super(str);
        }

        protected void ioAction() {
            BluetoothDevice remoteDevice = BluetoothLeService.this.mBluetoothAdapter.getRemoteDevice(this.val$address);
            if (remoteDevice == null) {
                Log.w(BluetoothLeService.TAG, "Device not found.  Unable to connect.");
                return;
            }
            BluetoothLeService.this.mConnections.put(this.val$address, remoteDevice.connectGatt(BluetoothLeService.this.getApplicationContext(), true, new BCallBack(this.val$address)));
            Log.d(BluetoothLeService.TAG, "Trying to create a new connection.");
            BluetoothLeService.this.mConnectionState.put(this.val$address, Integer.valueOf(BluetoothLeService.SUPER_FAST));
            BluetoothLeService.this.mTimer = new Timer("connectTimer");
            BluetoothLeService.this.mTimer.schedule(new 1(this), 30000);
        }
    }

    /* renamed from: com.starkey.bledevice.BluetoothLeService.2 */
    class AnonymousClass2 extends ReadWrite {
        final /* synthetic */ String val$address;
        final /* synthetic */ BluetoothGattCharacteristic val$characteristic;

        AnonymousClass2(String str, String str2, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            this.val$address = str2;
            this.val$characteristic = bluetoothGattCharacteristic;
            super(str);
        }

        protected void ioAction() {
            if (BluetoothLeService.this.mConnections.get(this.val$address) == null) {
                Log.i(BluetoothLeService.TAG, "read: not connected");
                BluetoothLeService.this.broadcastUpdate(this.val$address, BluetoothLeService.ACTION_GATT_READ_ERROR);
                BluetoothLeService.this.nextRequest();
                return;
            }
            Log.d("read", this.val$address);
            ((BluetoothGatt) BluetoothLeService.this.mConnections.get(this.val$address)).readCharacteristic(this.val$characteristic);
            BluetoothLeService.this.mTimer = new Timer("readTimer");
            BluetoothLeService.this.mTimer.schedule(new 1(this), 5000);
        }
    }

    /* renamed from: com.starkey.bledevice.BluetoothLeService.3 */
    class AnonymousClass3 extends ReadWrite {
        final /* synthetic */ String val$address;

        AnonymousClass3(String str, String str2) {
            this.val$address = str2;
            super(str);
        }

        protected void ioAction() {
            if (BluetoothLeService.this.mConnections.get(this.val$address) == null) {
                Log.w(BluetoothLeService.TAG, "read RSSI, not connected");
                BluetoothLeService.this.broadcastUpdate(this.val$address, BluetoothLeService.ACTION_READ_REMOTE_RSSI, (int) BluetoothLeService.STATE_DISCONNECTED);
                return;
            }
            ((BluetoothGatt) BluetoothLeService.this.mConnections.get(this.val$address)).readRemoteRssi();
            BluetoothLeService.this.mTimer = new Timer("writeTimer");
            BluetoothLeService.this.mTimer.schedule(new 1(this), 5000);
        }
    }

    /* renamed from: com.starkey.bledevice.BluetoothLeService.4 */
    class AnonymousClass4 extends ReadWrite {
        final /* synthetic */ String val$address;
        final /* synthetic */ BluetoothGattCharacteristic val$characteristic;
        final /* synthetic */ byte[] val$value;

        AnonymousClass4(String str, String str2, BluetoothGattCharacteristic bluetoothGattCharacteristic, byte[] bArr) {
            this.val$address = str2;
            this.val$characteristic = bluetoothGattCharacteristic;
            this.val$value = bArr;
            super(str);
        }

        protected void ioAction() {
            if (BluetoothLeService.this.mConnections.get(this.val$address) == null) {
                Log.w(BluetoothLeService.TAG, "write, not connected");
                BluetoothLeService.this.broadcastUpdate(this.val$address, BluetoothLeService.ACTION_GATT_WRITE_ERROR);
                return;
            }
            BluetoothLeService.this.writeCharacteristic(this.val$address, this.val$characteristic, this.val$value);
            BluetoothLeService.this.mTimer = new Timer("writeTimer");
            BluetoothLeService.this.mTimer.schedule(new 1(this), 5000);
        }
    }

    /* renamed from: com.starkey.bledevice.BluetoothLeService.5 */
    class AnonymousClass5 extends ReadWrite {
        final /* synthetic */ String val$address;
        final /* synthetic */ BluetoothGattCharacteristic val$characteristic;
        final /* synthetic */ boolean val$enabled;

        AnonymousClass5(String str, String str2, BluetoothGattCharacteristic bluetoothGattCharacteristic, boolean z) {
            this.val$address = str2;
            this.val$characteristic = bluetoothGattCharacteristic;
            this.val$enabled = z;
            super(str);
        }

        protected void ioAction() {
            if (BluetoothLeService.this.mConnections.get(this.val$address) == null) {
                Log.w(BluetoothLeService.TAG, "setNotify, not initialized");
                BluetoothLeService.this.broadcastUpdate(this.val$address, BluetoothLeService.ACTION_GATT_SET_NOTIFY_ERROR);
                return;
            }
            BluetoothLeService.this.setCharacteristicNotificationTask(this.val$address, this.val$characteristic, this.val$enabled);
            Log.d("set notify", this.val$address);
            BluetoothLeService.this.mTimer = new Timer("setnotifyTimer");
            BluetoothLeService.this.mTimer.schedule(new 1(this), BeaconManager.DEFAULT_BACKGROUND_SCAN_PERIOD);
        }
    }

    /* renamed from: com.starkey.bledevice.BluetoothLeService.6 */
    class AnonymousClass6 extends ReadWrite {
        final /* synthetic */ String val$address;
        final /* synthetic */ BluetoothGattCharacteristic val$lastcharacteristic;
        final /* synthetic */ BluetoothGattCharacteristic val$nextcharacteristic;
        final /* synthetic */ byte[] val$value;

        AnonymousClass6(String str, String str2, byte[] bArr, BluetoothGattCharacteristic bluetoothGattCharacteristic, BluetoothGattCharacteristic bluetoothGattCharacteristic2) {
            this.val$address = str2;
            this.val$value = bArr;
            this.val$nextcharacteristic = bluetoothGattCharacteristic;
            this.val$lastcharacteristic = bluetoothGattCharacteristic2;
            super(str);
        }

        protected void ioAction() {
            if (BluetoothLeService.this.isConnected(this.val$address)) {
                BluetoothLeService.this.exchangeHearingAidDataTask(this.val$address, this.val$value, this.val$nextcharacteristic, this.val$lastcharacteristic, false);
                BluetoothLeService.this.mTimer = new Timer("writeTimer");
                BluetoothLeService.this.mTimer.schedule(new 1(this), 15000);
                return;
            }
            Log.w(BluetoothLeService.TAG, "exchange data,  not connected " + this.val$address);
            BluetoothLeService.this.broadcastUpdate(this.val$address, BluetoothLeService.ACTION_GATT_SSI_ERROR);
        }
    }

    /* renamed from: com.starkey.bledevice.BluetoothLeService.7 */
    class AnonymousClass7 extends ReadWrite {
        final /* synthetic */ String val$address;
        final /* synthetic */ BluetoothGattCharacteristic val$lastcharacteristic;
        final /* synthetic */ BluetoothGattCharacteristic val$nextcharacteristic;
        final /* synthetic */ byte[] val$value;

        AnonymousClass7(String str, String str2, byte[] bArr, BluetoothGattCharacteristic bluetoothGattCharacteristic, BluetoothGattCharacteristic bluetoothGattCharacteristic2) {
            this.val$address = str2;
            this.val$value = bArr;
            this.val$nextcharacteristic = bluetoothGattCharacteristic;
            this.val$lastcharacteristic = bluetoothGattCharacteristic2;
            super(str);
        }

        protected void ioAction() {
            if (BluetoothLeService.this.isConnected(this.val$address)) {
                BluetoothLeService.this.exchangeHearingAidDataTask(this.val$address, this.val$value, this.val$nextcharacteristic, this.val$lastcharacteristic, true);
                BluetoothLeService.this.mTimer = new Timer("transmitTimer");
                BluetoothLeService.this.mTimer.schedule(new 1(this), 15000);
                return;
            }
            Log.w(BluetoothLeService.TAG, "transmitSSI, not connected " + this.val$address);
            BluetoothLeService.this.broadcastUpdate(this.val$address, BluetoothLeService.ACTION_GATT_SSI_ERROR);
        }
    }

    /* renamed from: com.starkey.bledevice.BluetoothLeService.9 */
    class AnonymousClass9 extends ReadWrite {
        final /* synthetic */ BluetoothGattCharacteristic val$SSINext;
        final /* synthetic */ String val$address;
        final /* synthetic */ byte[] val$transferBuffer;

        AnonymousClass9(String str, String str2, BluetoothGattCharacteristic bluetoothGattCharacteristic, byte[] bArr) {
            this.val$address = str2;
            this.val$SSINext = bluetoothGattCharacteristic;
            this.val$transferBuffer = bArr;
            super(str);
        }

        protected void ioAction() {
            BluetoothLeService.this.writeCharacteristic(this.val$address, this.val$SSINext, this.val$transferBuffer);
        }
    }

    private final class BCallBack extends BluetoothGattCallback {
        private String mAddress;
        private boolean mSettingNotifications;

        public BCallBack(String str) {
            this.mAddress = str;
        }

        public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            Log.d(BluetoothLeService.TAG, "OnCharacteristicChanged " + bluetoothGattCharacteristic.getUuid().toString());
            if (BluetoothLeService.this.mSSIResponseHandler == null || !BluetoothLeService.this.mSSIResponseHandler.inProgress()) {
                BluetoothLeService.this.broadcastUpdate(this.mAddress, BluetoothLeService.ACTION_CHAR_UPDATED, bluetoothGattCharacteristic);
                return;
            }
            Log.i(BluetoothLeService.TAG, "onCharacteristicChanged: SSI" + bluetoothGattCharacteristic.getUuid().toString());
            BluetoothLeService.this.mSSIResponseHandler.handleSSICharacteristicUpdated(this.mAddress, bluetoothGattCharacteristic);
        }

        public void onCharacteristicRead(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            if (i == 0 || i == 137) {
                Log.d(BluetoothLeService.TAG, "OnCharacteristicRead " + bluetoothGattCharacteristic.getUuid().toString());
                BluetoothLeService.this.broadcastUpdate(this.mAddress, BluetoothLeService.ACTION_DATA_AVAILABLE, bluetoothGattCharacteristic);
                BluetoothLeService.mIsBusy = false;
                BluetoothLeService.this.nextRequest();
            }
            if (i == 137) {
                Log.d(BluetoothLeService.TAG, "Status 5/137 received");
                BluetoothLeService.this.broadcastUpdate(this.mAddress, BluetoothLeService.ACTION_STATUS5_RECEIVED, bluetoothGattCharacteristic);
            }
            if (i == 5) {
                Log.d(BluetoothLeService.TAG, "Status 5 ");
                Log.d(BluetoothLeService.TAG, "Bond state is " + bluetoothGatt.getDevice().getBondState());
            }
        }

        public void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            if (i == 0 || i == 137) {
                if (i == 137) {
                    Log.d(BluetoothLeService.TAG, "STATUS 137");
                }
                if (BluetoothLeService.this.mSSIResponseHandler == null || !BluetoothLeService.this.mSSIResponseHandler.inProgress()) {
                    BluetoothLeService.this.broadcastUpdate(this.mAddress, BluetoothLeService.ACTION_CHARACTERISTIC_WROTE, bluetoothGattCharacteristic);
                    BluetoothLeService.mIsBusy = false;
                    BluetoothLeService.this.nextRequest();
                    return;
                }
                Log.d(BluetoothLeService.TAG, "onCharacteristicWrite: Processed SSI characeteristic");
                BluetoothLeService.this.mSSIResponseHandler.handleSSICharacteristicWrote(this.mAddress, bluetoothGattCharacteristic);
            }
        }

        public void onConnectionStateChange(BluetoothGatt bluetoothGatt, int i, int i2) {
            if (i2 == BluetoothLeService.SUPER_SLOW) {
                if (i == 0) {
                    BluetoothLeService.this.mConnectionState.put(this.mAddress, Integer.valueOf(BluetoothLeService.SUPER_SLOW));
                    BluetoothLeService.this.broadcastUpdate(this.mAddress, BluetoothLeService.ACTION_GATT_CONNECTED);
                    Log.i(BluetoothLeService.TAG, "Connected to GATT server.");
                    Log.i(BluetoothLeService.TAG, "Attempting to start service discovery:" + bluetoothGatt.discoverServices());
                    return;
                }
                Log.i(BluetoothLeService.TAG, "Connection to GATT server not successful.");
                BluetoothLeService.this.broadcastUpdate(this.mAddress, BluetoothLeService.ACTION_GATT_CONNECT_FAILED);
                BluetoothLeService.mIsBusy = false;
                BluetoothLeService.this.nextRequest();
            } else if (i2 == 0) {
                BluetoothLeService.this.mConnectionState.put(this.mAddress, Integer.valueOf(BluetoothLeService.STATE_DISCONNECTED));
                Log.i(BluetoothLeService.TAG, "Disconnected from GATT server.");
                BluetoothLeService.this.broadcastUpdate(this.mAddress, BluetoothLeService.ACTION_GATT_DISCONNECTED);
                BluetoothLeService.this.clearQueue(this.mAddress);
                BluetoothLeService.this.close(this.mAddress);
                if (BluetoothLeService.this.mDisconnectCalled.containsKey(this.mAddress) && ((Boolean) BluetoothLeService.this.mDisconnectCalled.get(this.mAddress)).booleanValue()) {
                    BluetoothLeService.this.mDisconnectCalled.put(this.mAddress, Boolean.valueOf(false));
                } else if (BluetoothLeService.this.mBluetoothAdapter.isEnabled()) {
                    BluetoothLeService.this.connect(this.mAddress);
                }
            }
        }

        public void onDescriptorWrite(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor bluetoothGattDescriptor, int i) {
            if (i != 0) {
                BluetoothLeService.this.broadcastUpdate(this.mAddress, BluetoothLeService.ACTION_GATT_SET_NOTIFY_ERROR);
                Log.d(BluetoothLeService.TAG, "OnDescriptorWrite Status " + i);
            } else if (this.mSettingNotifications) {
                BluetoothGattCharacteristic gattCharacteristic = BluetoothLeService.this.getGattCharacteristic(this.mAddress, SHIPGattAttributes.SHIP_SERVICE, SHIPGattAttributes.SSINext);
                BluetoothGattCharacteristic gattCharacteristic2 = BluetoothLeService.this.getGattCharacteristic(this.mAddress, SHIPGattAttributes.SHIP_SERVICE, SHIPGattAttributes.SSILast);
                if (bluetoothGattDescriptor.getCharacteristic().equals(gattCharacteristic)) {
                    BluetoothLeService.this.setCharacteristicNotificationTask(this.mAddress, gattCharacteristic2, true);
                    return;
                }
                this.mSettingNotifications = false;
                BluetoothLeService.this.broadcastUpdate(this.mAddress, BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
                BluetoothLeService.mIsBusy = false;
                BluetoothLeService.this.nextRequest();
            } else {
                BluetoothLeService.this.broadcastUpdate(this.mAddress, BluetoothLeService.ACTION_DESCRIPTOR_WROTE, bluetoothGattDescriptor.getCharacteristic());
                BluetoothLeService.mIsBusy = false;
                BluetoothLeService.this.nextRequest();
            }
        }

        public void onReadRemoteRssi(BluetoothGatt bluetoothGatt, int i, int i2) {
            Log.i(BluetoothLeService.TAG, "onReadRemoteRssi: ");
            if (i2 == 0) {
                BluetoothLeService.this.broadcastUpdate(this.mAddress, BluetoothLeService.ACTION_READ_REMOTE_RSSI, i);
                BluetoothLeService.mIsBusy = false;
                BluetoothLeService.this.nextRequest();
                return;
            }
            Log.i(BluetoothLeService.TAG, "onReadRemoteRssi: error occurred");
            BluetoothLeService.this.broadcastUpdate(this.mAddress, BluetoothLeService.ACTION_READ_REMOTE_RSSI, (int) BluetoothLeService.STATE_DISCONNECTED);
            BluetoothLeService.mIsBusy = false;
            BluetoothLeService.this.nextRequest();
        }

        public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int i) {
            Log.w(BluetoothLeService.TAG, "onServicesDiscovered received: " + i);
            if (i == 0) {
                BluetoothGattCharacteristic gattCharacteristic = BluetoothLeService.this.getGattCharacteristic(this.mAddress, SHIPGattAttributes.SHIP_SERVICE, SHIPGattAttributes.SSINext);
                if (gattCharacteristic != null) {
                    this.mSettingNotifications = true;
                    BluetoothLeService.this.setCharacteristicNotificationTask(this.mAddress, gattCharacteristic, true);
                    return;
                }
                BluetoothLeService.this.broadcastUpdate(this.mAddress, BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
                BluetoothLeService.mIsBusy = false;
                BluetoothLeService.this.nextRequest();
            }
        }
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this.initialize() ? BluetoothLeService.this : null;
        }
    }

    static {
        TAG = BluetoothLeService.class.getSimpleName();
        mReadWriteQueue = new ConcurrentLinkedQueue();
        mIsBusy = false;
    }

    public BluetoothLeService() {
        this.mConnections = new HashMap();
        this.mConnectionState = new HashMap();
        this.mDisconnectCalled = new HashMap();
        this.mBinder = new LocalBinder();
    }

    private void broadcastUpdate(String str) {
        sendBroadcast(new Intent(str));
    }

    private void broadcastUpdate(String str, String str2) {
        Intent intent = new Intent(str2);
        intent.putExtra(ADDRESS, str);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(String str, String str2, int i) {
        Intent intent = new Intent(str2);
        intent.putExtra(ADDRESS, str);
        intent.putExtra(EXTRA_RSSI, i);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(String str, String str2, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        Intent intent = new Intent(str2);
        intent.putExtra(ADDRESS, str);
        if (bluetoothGattCharacteristic != null) {
            byte[] value = bluetoothGattCharacteristic.getValue();
            intent.putExtra(EXTRA_BYTE_DATA, value);
            if (value != null && value.length > 0) {
                StringBuilder stringBuilder = new StringBuilder(value.length);
                int length = value.length;
                for (int i = STATE_DISCONNECTED; i < length; i += SUPER_FAST) {
                    Object[] objArr = new Object[SUPER_FAST];
                    objArr[STATE_DISCONNECTED] = Byte.valueOf(value[i]);
                    stringBuilder.append(String.format("%02X ", objArr));
                }
                intent.putExtra(EXTRA_STRING_DATA, stringBuilder.toString());
            }
            intent.putExtra(EXTRA_STRING_CHARACTERISTIC, bluetoothGattCharacteristic.getUuid().toString());
        }
        sendBroadcast(intent);
    }

    private void broadcastUpdate(String str, String str2, byte[] bArr) {
        Intent intent = new Intent(str2);
        intent.putExtra(ADDRESS, str);
        intent.putExtra(EXTRA_BYTE_DATA, bArr);
        sendBroadcast(intent);
    }

    private void clearQueue(String str) {
        synchronized (this) {
            for (ReadWrite readWrite : mReadWriteQueue) {
                Log.d(TAG, "clearQueue: r address is " + readWrite.getAddress());
                Log.d(TAG, "clearQueue: address is " + str);
                if ((readWrite != null && readWrite.getAddress() == null) || readWrite.getAddress().equalsIgnoreCase(str)) {
                    mReadWriteQueue.remove(readWrite);
                }
            }
        }
    }

    private void doRequest(ReadWrite readWrite) {
        synchronized (this) {
            Log.d(TAG, "doRequest: ");
            if (readWrite != null) {
                mIsBusy = true;
                readWrite.ioAction();
            } else {
                nextRequest();
            }
        }
    }

    private void exchangeHearingAidDataTask(String str, byte[] bArr, BluetoothGattCharacteristic bluetoothGattCharacteristic, BluetoothGattCharacteristic bluetoothGattCharacteristic2, boolean z) {
        Object obj;
        this.mSSIResponseHandler = new SSIResponseHandler(str, bluetoothGattCharacteristic, bluetoothGattCharacteristic2, new SSIResponseCallback() {
            public void handleError(String str) {
                Log.d(BluetoothLeService.TAG, "onCharacteristicWrite: Missing SSILast");
                BluetoothLeService.this.broadcastUpdate(str, BluetoothLeService.ACTION_GATT_SSI_ERROR);
                BluetoothLeService.mIsBusy = false;
                BluetoothLeService.this.nextRequest();
            }

            public void sendReturnData(String str, byte[] bArr) {
                Log.i(BluetoothLeService.TAG, "handleSSICharacteristicUpdated:  broadcasting " + Arrays.toString(bArr));
                BluetoothLeService.this.broadcastUpdate(str, BluetoothLeService.ACTION_SSI_DATA_AVAILABLE, bArr);
                BluetoothLeService.mIsBusy = false;
                BluetoothLeService.this.nextRequest();
            }
        });
        Log.i(TAG, "Attempting to read using SSI command: " + str);
        Log.i(TAG, Arrays.toString(bArr));
        int i = SHIPGattAttributes.BLE_TRANSFER_SIZE;
        int length = bArr.length / i;
        int i2 = bArr.length % i != 0 ? length + SUPER_FAST : length;
        for (int i3 = STATE_DISCONNECTED; i3 < i2 - 1; i3 += SUPER_FAST) {
            obj = new byte[i];
            System.arraycopy(bArr, i3 * i, obj, STATE_DISCONNECTED, i);
            this.mSSIResponseHandler.add(new AnonymousClass9(str, str, bluetoothGattCharacteristic, obj));
        }
        length = bArr.length - ((i2 - 1) * i);
        obj = new byte[length];
        System.arraycopy(bArr, bArr.length - length, obj, STATE_DISCONNECTED, length);
        this.mSSIResponseHandler.add(new AnonymousClass10(str, str, bluetoothGattCharacteristic2, obj, z));
        this.mSSIResponseHandler.startTransmit();
    }

    private void nextRequest() {
        synchronized (this) {
            Log.d(TAG, "nextRequest: ");
            mIsBusy = false;
            if (this.mTimer != null) {
                this.mTimer.cancel();
            }
            if (!(mReadWriteQueue.isEmpty() || mIsBusy)) {
                doRequest((ReadWrite) mReadWriteQueue.poll());
            }
        }
    }

    private void requestIO(ReadWrite readWrite) {
        synchronized (this) {
            Log.d(TAG, "requestIO: ");
            if (!mReadWriteQueue.isEmpty() || mIsBusy) {
                mReadWriteQueue.add(readWrite);
            } else {
                doRequest(readWrite);
            }
        }
    }

    private void setCharacteristicNotificationTask(String str, BluetoothGattCharacteristic bluetoothGattCharacteristic, boolean z) {
        ((BluetoothGatt) this.mConnections.get(str)).setCharacteristicNotification(bluetoothGattCharacteristic, z);
        BluetoothGattDescriptor descriptor = bluetoothGattCharacteristic.getDescriptor(UUID.fromString(SHIPGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        ((BluetoothGatt) this.mConnections.get(str)).writeDescriptor(descriptor);
    }

    private void writeCharacteristic(String str, BluetoothGattCharacteristic bluetoothGattCharacteristic, byte[] bArr) {
        if (isConnected(str)) {
            bluetoothGattCharacteristic.setValue(bArr);
            bluetoothGattCharacteristic.setWriteType(SUPER_SLOW);
            ((BluetoothGatt) this.mConnections.get(str)).writeCharacteristic(bluetoothGattCharacteristic);
            Log.d("write", str);
        }
    }

    public void bondDevice(String str) {
        this.mBluetoothAdapter.getRemoteDevice(str).createBond();
    }

    public void close(String str) {
        if (this.mConnections.get(str) != null) {
            BluetoothGatt bluetoothGatt = (BluetoothGatt) this.mConnections.get(str);
            this.mConnections.remove(str);
            bluetoothGatt.close();
        }
    }

    public boolean connect(String str) {
        if (this.mBluetoothAdapter == null || str == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        if (this.mConnections.containsKey(str)) {
            this.mConnections.remove(str);
        }
        requestIO(new AnonymousClass1(str, str));
        return true;
    }

    public boolean deviceBonded(String str) {
        return this.mBluetoothAdapter.getRemoteDevice(str).getBondState() == 12;
    }

    public void disconnect(String str) {
        if (this.mBluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
        } else if (this.mConnections.get(str) == null) {
            Log.w(TAG, "disconnect: device is null");
        } else {
            this.mDisconnectCalled.put(str, Boolean.valueOf(true));
            ((BluetoothGatt) this.mConnections.get(str)).disconnect();
        }
    }

    public void exchangeHearingAidData(String str, byte[] bArr, String str2, String str3, String str4) {
        if (this.mBluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            broadcastUpdate(str, ACTION_GATT_SSI_ERROR);
            return;
        }
        BluetoothGattCharacteristic gattCharacteristic = getGattCharacteristic(str, str2, str3);
        if (gattCharacteristic == null) {
            Log.d(TAG, "exchangeHearingAidData: Error, characteristic not found: " + str3);
            broadcastUpdate(str, ACTION_GATT_SSI_ERROR);
            return;
        }
        BluetoothGattCharacteristic gattCharacteristic2 = getGattCharacteristic(str, str2, str4);
        if (gattCharacteristic2 == null) {
            Log.d(TAG, "exchangeHearingAidData: Error, characteristic not found: " + str4);
            broadcastUpdate(str, ACTION_GATT_SSI_ERROR);
            return;
        }
        requestIO(new AnonymousClass6(str, str, bArr, gattCharacteristic, gattCharacteristic2));
    }

    public BluetoothGattCharacteristic getGattCharacteristic(String str, String str2, String str3) {
        if (this.mConnections.get(str) == null) {
            return null;
        }
        if (str2 == null) {
            str2 = SHIPGattAttributes.SHIP_SERVICE;
        }
        BluetoothGattService service = ((BluetoothGatt) this.mConnections.get(str)).getService(UUID.fromString(str2));
        return service == null ? null : service.getCharacteristic(UUID.fromString(str3));
    }

    public List<String> getSupportedGattServices(String str) {
        if (this.mConnections.get(str) == null) {
            return null;
        }
        List<BluetoothGattService> services = ((BluetoothGatt) this.mConnections.get(str)).getServices();
        if (services == null) {
            return null;
        }
        List<String> arrayList = new ArrayList();
        for (BluetoothGattService uuid : services) {
            arrayList.add(uuid.getUuid().toString());
        }
        return arrayList;
    }

    public boolean initialize() {
        if (this.mBluetoothManager == null) {
            this.mBluetoothManager = (BluetoothManager) getSystemService("bluetooth");
            if (this.mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        this.mBluetoothAdapter = this.mBluetoothManager.getAdapter();
        if (this.mBluetoothAdapter != null) {
            return true;
        }
        Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
        return false;
    }

    public boolean isConnected(String str) {
        return ((Integer) this.mConnectionState.get(str)).intValue() == SUPER_SLOW;
    }

    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    public boolean onUnbind(Intent intent) {
        close(intent.getStringExtra(EXTRA_STRING_DATA));
        return super.onUnbind(intent);
    }

    public void readCharacteristic(String str, String str2, String str3) {
        if (this.mBluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            broadcastUpdate(str, ACTION_GATT_READ_ERROR);
            return;
        }
        BluetoothGattCharacteristic gattCharacteristic = getGattCharacteristic(str, str2, str3);
        if (gattCharacteristic == null) {
            Log.d(TAG, "readCharacteristic: Error, characteristic not found: " + str3);
            broadcastUpdate(str, ACTION_GATT_READ_ERROR);
            return;
        }
        requestIO(new AnonymousClass2(str, str, gattCharacteristic));
    }

    public void readRSSI(String str) {
        if (this.mBluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            broadcastUpdate(str, ACTION_READ_REMOTE_RSSI, (int) STATE_DISCONNECTED);
        } else if (isConnected(str)) {
            requestIO(new AnonymousClass3(str, str));
        } else {
            Log.w(TAG, "ReadRSSI not being called, not connected " + str);
            broadcastUpdate(str, ACTION_READ_REMOTE_RSSI, (int) STATE_DISCONNECTED);
        }
    }

    public boolean requestConnectionPriorityChange(String str, int i) {
        if (VERSION.SDK_INT < 21) {
            return false;
        }
        Log.d(TAG, "Requesting connection speed change to " + i);
        boolean requestConnectionPriority = ((BluetoothGatt) this.mConnections.get(str)).requestConnectionPriority(i);
        try {
            Thread.sleep(400);
        } catch (Exception e) {
            Log.d(TAG, "Exception calling thread sleep on connection priority change");
        }
        if (requestConnectionPriority) {
            return requestConnectionPriority;
        }
        Log.d(TAG, "Denied connection speed change");
        return requestConnectionPriority;
    }

    public void setCharacteristicNotification(String str, String str2, String str3, boolean z) {
        if (this.mBluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            broadcastUpdate(str, ACTION_GATT_SET_NOTIFY_ERROR);
            return;
        }
        BluetoothGattCharacteristic gattCharacteristic = getGattCharacteristic(str, str2, str3);
        if (gattCharacteristic == null) {
            Log.d(TAG, "setCharacteristicNotification: Error, characteristic not found: " + str3);
            broadcastUpdate(str, ACTION_GATT_SET_NOTIFY_ERROR);
            return;
        }
        requestIO(new AnonymousClass5(str, str, gattCharacteristic, z));
    }

    public void transmitSSI(String str, byte[] bArr, String str2, String str3, String str4) {
        if (this.mBluetoothAdapter == null || this.mConnections.get(str) == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unconnected");
            broadcastUpdate(str, ACTION_GATT_SSI_ERROR);
            return;
        }
        BluetoothGattCharacteristic gattCharacteristic = getGattCharacteristic(str, str2, str3);
        if (gattCharacteristic == null) {
            Log.d(TAG, "transmitSSI: Error, characteristic not found: " + str3);
            broadcastUpdate(str, ACTION_GATT_SSI_ERROR);
            return;
        }
        BluetoothGattCharacteristic gattCharacteristic2 = getGattCharacteristic(str, str2, str4);
        if (gattCharacteristic2 == null) {
            Log.d(TAG, "transmitSSI: Error, characteristic not found: " + str4);
            broadcastUpdate(str, ACTION_GATT_SSI_ERROR);
            return;
        }
        requestIO(new AnonymousClass7(str, str, bArr, gattCharacteristic, gattCharacteristic2));
    }

    public void writeCharacteristic(String str, String str2, String str3, byte[] bArr) {
        if (this.mBluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            broadcastUpdate(str, ACTION_GATT_WRITE_ERROR);
            return;
        }
        BluetoothGattCharacteristic gattCharacteristic = getGattCharacteristic(str, str2, str3);
        if (gattCharacteristic == null) {
            Log.d(TAG, "writeCharacteristic: Error, characteristic not found: " + str3);
            broadcastUpdate(str, ACTION_GATT_WRITE_ERROR);
            return;
        }
        requestIO(new AnonymousClass4(str, str, gattCharacteristic, bArr));
    }
}
