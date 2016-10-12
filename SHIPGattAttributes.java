package com.starkey.bledevice;

import com.starkey.trulink.android.R;
import java.util.HashMap;

public class SHIPGattAttributes {
    public static String AUDIO_CONFIGURATION;
    public static String BATTERY_LEVEL;
    public static int BLE_TRANSFER_SIZE;
    public static String CLIENT_CHARACTERISTIC_CONFIG;
    public static String DEVICE_INFORMATION_SERVICE;
    public static String HEARING_AID_ID;
    public static String MANUFACTURER_NAME_STRING;
    public static String MEMORY_SELECTION;
    public static String MODEL_NUMBER_STRING;
    public static String OTHER_HEARING_AID_ID;
    public static String SHIP_SERVICE;
    public static String SIDE;
    public static String SSILast;
    public static String SSINext;
    public static int SSI_MAX_BUFFER;
    public static int[] ShipBytes;
    public static String _activeStreamIndicatorCharacteristic;
    public static String _availableProgramsBitmask;
    public static String _firmwareRevisionCharacteristic;
    public static String _selectedProgramID;
    public static String _selectedProgramName;
    public static String _versionNumbers;
    private static HashMap<String, String> attributes;

    static {
        attributes = new HashMap();
        CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
        BATTERY_LEVEL = "896C9720-D4EA-11E1-AF47-58B035FEA743";
        SIDE = "896C990A-D4EA-11E1-AF54-58B035FEA743";
        OTHER_HEARING_AID_ID = "896C9950-D4EA-11E1-AF56-58B035FEA743";
        HEARING_AID_ID = "896C9932-D4EA-11E1-AF55-58B035FEA743";
        _availableProgramsBitmask = "896C9978-D4EA-11E1-AF57-58B035FEA743";
        _selectedProgramID = "896C9874-D4EA-11E1-AF50-58B035FEA743";
        _selectedProgramName = "896C9770-D4EA-11E1-AF49-58B035FEA743";
        SHIP_SERVICE = "896C9518-D4EA-11E1-AF45-58B035FEA743";
        ShipBytes = new int[]{67, 167, 254, 53, 176, 88, 69, 175, 225, 17, 234, 212, 24, 149, R.styleable.AppCompatTheme_ratingBarStyleSmall, 137};
        AUDIO_CONFIGURATION = "896C9748-D4EA-11E1-AF48-58B035FEA743";
        _versionNumbers = "896C96EE-D4EA-11E1-AF46-58B035FEA743";
        MEMORY_SELECTION = "896C978E-D4EA-11E1-AF4A-58B035FEA743";
        DEVICE_INFORMATION_SERVICE = "0000180A-0000-1000-8000-00805F9B34FB";
        _firmwareRevisionCharacteristic = "00002A26-0000-1000-8000-00805F9B34FB";
        MANUFACTURER_NAME_STRING = "00002A29-0000-1000-8000-00805F9B34FB";
        MODEL_NUMBER_STRING = "00002a24-0000-1000-8000-00805f9b34fb";
        _activeStreamIndicatorCharacteristic = "896C98E2-D4EA-11E1-AF53-58B035FEA743";
        SSILast = "896c97de-d4ea-11e1-af4c-58b035fea743";
        SSINext = "896c97b6-d4ea-11e1-af4b-58b035fea743";
        attributes.put(DEVICE_INFORMATION_SERVICE, "Device Information Service");
        attributes.put(MANUFACTURER_NAME_STRING, "Manufacturer Name");
        attributes.put(MODEL_NUMBER_STRING, "Model Number");
        attributes.put(SHIP_SERVICE, "SHIP Service");
        attributes.put(SIDE, "Side");
        attributes.put(HEARING_AID_ID, "Hearing Aid ID");
        attributes.put(OTHER_HEARING_AID_ID, "Other Hearing Aid ID");
        attributes.put(AUDIO_CONFIGURATION, "Audio Configuration");
        attributes.put(MEMORY_SELECTION, "Memory Selection");
        attributes.put(BATTERY_LEVEL, "Battery level");
        attributes.put(SSINext, "SSI Next");
        attributes.put(SSILast, "SSI Last");
        BLE_TRANSFER_SIZE = 20;
        SSI_MAX_BUFFER = 524;
    }

    public static String lookup(String str, String str2) {
        String str3 = (String) attributes.get(str);
        return str3 == null ? str2 : str3;
    }
}
