package com.starkey.bledevice;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;
import java.util.LinkedList;
import java.util.Queue;

public class SSIResponseHandler {
    private static final String TAG;
    private String mAddress;
    private SSIResponseCallback mCallback;
    private boolean mInProgress;
    private String mLastChar;
    private String mNextChar;
    private byte[] mReturnBuffer;
    boolean mSSILastUpdated;
    boolean mSSILastWrote;
    private Queue<ReadWrite> mSSIQueue;
    byte[] mSSIResponseBuffer;
    int mSSIResponseBufferPosition;

    static {
        TAG = SSIResponseHandler.class.getSimpleName();
    }

    public SSIResponseHandler(String str, BluetoothGattCharacteristic bluetoothGattCharacteristic, BluetoothGattCharacteristic bluetoothGattCharacteristic2, SSIResponseCallback sSIResponseCallback) {
        this.mSSILastUpdated = false;
        this.mSSILastWrote = false;
        this.mSSIResponseBufferPosition = 0;
        this.mSSIResponseBuffer = new byte[SHIPGattAttributes.SSI_MAX_BUFFER];
        this.mSSIQueue = new LinkedList();
        this.mNextChar = bluetoothGattCharacteristic.getUuid().toString();
        this.mLastChar = bluetoothGattCharacteristic2.getUuid().toString();
        this.mAddress = str;
        this.mInProgress = true;
        this.mCallback = sSIResponseCallback;
    }

    private void updateBuffer(byte[] bArr) {
        System.arraycopy(bArr, 0, this.mSSIResponseBuffer, this.mSSIResponseBufferPosition, bArr.length);
        this.mSSIResponseBufferPosition += bArr.length;
    }

    public void add(ReadWrite readWrite) {
        this.mSSIQueue.add(readWrite);
    }

    public void handleSSICharacteristicUpdated(String str, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        if (str.equalsIgnoreCase(this.mAddress) && this.mInProgress) {
            Log.i(TAG, "handleSSICharacteristicUpdated: " + this.mAddress + " " + bluetoothGattCharacteristic.getUuid().toString());
            if (bluetoothGattCharacteristic.getUuid().toString().equalsIgnoreCase(this.mLastChar)) {
                this.mSSILastUpdated = true;
                Log.i(TAG, "handleSSICharacteristicUpdated: SSILast");
                updateBuffer(bluetoothGattCharacteristic.getValue());
                this.mReturnBuffer = new byte[this.mSSIResponseBufferPosition];
                System.arraycopy(this.mSSIResponseBuffer, 0, this.mReturnBuffer, 0, this.mSSIResponseBufferPosition);
                if (this.mSSILastWrote) {
                    this.mInProgress = false;
                    this.mCallback.sendReturnData(this.mAddress, this.mReturnBuffer);
                }
            } else if (bluetoothGattCharacteristic.getUuid().toString().equalsIgnoreCase(this.mNextChar)) {
                updateBuffer(bluetoothGattCharacteristic.getValue());
            }
        }
    }

    public void handleSSICharacteristicWrote(String str, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        if (!str.equalsIgnoreCase(this.mAddress) || !this.mInProgress) {
            return;
        }
        if (bluetoothGattCharacteristic.getUuid().toString().equalsIgnoreCase(this.mNextChar)) {
            ReadWrite readWrite = (ReadWrite) this.mSSIQueue.poll();
            if (readWrite != null) {
                readWrite.ioAction();
            } else {
                this.mCallback.handleError(this.mAddress);
            }
        } else if (bluetoothGattCharacteristic.getUuid().toString().equalsIgnoreCase(this.mLastChar)) {
            Log.d(TAG, "onCharacteristicWrite: SSILast. This sometimes comes in after response");
            Log.d(TAG, "onCharacteristicWrite: SSILast. Is this from hearing aid?");
            this.mSSILastWrote = true;
            if (this.mSSILastUpdated) {
                this.mInProgress = false;
                this.mCallback.sendReturnData(this.mAddress, this.mReturnBuffer);
            }
        }
    }

    public boolean inProgress() {
        return this.mInProgress;
    }

    public void startTransmit() {
        ((ReadWrite) this.mSSIQueue.poll()).ioAction();
    }
}
