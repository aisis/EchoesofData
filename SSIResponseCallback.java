package com.starkey.bledevice;

public abstract class SSIResponseCallback {
    public abstract void handleError(String str);

    public abstract void sendReturnData(String str, byte[] bArr);
}
