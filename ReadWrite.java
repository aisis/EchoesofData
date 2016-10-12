package com.starkey.bledevice;

public abstract class ReadWrite {
    private String mAddress;

    public ReadWrite(String str) {
        this.mAddress = this.mAddress;
    }

    public String getAddress() {
        return this.mAddress;
    }

    protected abstract void ioAction();
}
