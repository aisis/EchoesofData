package com.starkey.bledevice;

public interface BLEScanner {
    void startSearchingForDevices(String str, BLEScanCallBack bLEScanCallBack);

    void stopSearchingForDevices();
}
