package com.sonymobile.ibeaconservice.Utils;

import android.annotation.TargetApi;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.Build;

import java.nio.ByteBuffer;

@TargetApi(Build.VERSION_CODES.O)
public class IBeaconUtils {
    private static BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private AdvertiseCallback mAdvertiseCallback;

    private static final int MANUFACTURER_ID = 0x004c;

    public IBeaconUtils() {
        mBluetoothLeAdvertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();

        mAdvertiseCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);

                switch(errorCode){
                    case AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED:
                        mBluetoothLeAdvertiser.stopAdvertising(this);
                        break;
                    case ADVERTISE_FAILED_DATA_TOO_LARGE:
                        //Toast.makeText(mActivity, "Failed. Advertising data too large. Try again", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        System.out.println("towa on start failure: " + errorCode);
                        break;
                }
            }

        };
    }

    /**
     * byte[0] 0x02 iBeacon product id
     * byte[1] 0x15 data length. 20 bytes of manufacturer data + TX power
     * byte[2]-[21] Manufacturer data
     * byte[22] TX Power
     * @return 23 bytes as described above
     */
    public static ByteBuffer createDefaultIBeaconAdvertisement() {
        //android gives us no direct control over the first 7 bytes
        //Byte 0 should be a length of 2
        //Byte 1 and 2 are flags
        //Byte 3 is the remaining length of the record (26)
        //Byte 4 is ff denoting manufacturer specific data
        //Byte 5 & 6 are the Manufacturer id (0x004C)
        //So, actual byte positions below are offset by 9
        ByteBuffer adData = ByteBuffer.allocate(23);

        adData.put( 0,(byte)0x02);//iBeacon product ID
        adData.put( 1,(byte)0x15);//0x15 data length (21)
        adData.put( 2,(byte)0xd3);//start proximity UUID
        adData.put( 3,(byte)0xcb);
        adData.put( 4,(byte)0xd6);
        adData.put( 5,(byte)0xaa);
        adData.put( 6,(byte)0xaa);
        adData.put( 7,(byte)0xaa);
        adData.put( 8,(byte)0xaa);
        adData.put( 9,(byte)0xaa);
        adData.put(10,(byte)0xaa);
        adData.put(11,(byte)0xaa);
        adData.put(12,(byte)0xaa);
        adData.put(13,(byte)0xaa);
        adData.put(14,(byte)0xaa);
        adData.put(15,(byte)0x1f);//end proximity UUID
        adData.put(16,(byte)0xee);//Batt voltage 1
        adData.put(17,(byte)0xee);//Batt voltage 2
        adData.put(18,(byte)0xfe);// major 1
        adData.put(19,(byte)0xef);// major 2
        adData.put(20,(byte)0xaf);//minor 1
        adData.put(21,(byte)0xfa);//minor 2
        adData.put(22,(byte)0xc5); //signal power (-58? in two's complement)

        return adData.duplicate();
    }

    private static AdvertiseData setAdvertiseData(ByteBuffer adData) {
        //android gives us no control over the initial bytes.
        //(flag length, flags, manufacturer data, payload length, 0xff)
        //so in effect we are only setting from byte 8 onwards
        AdvertiseData.Builder advertiseDataBuilder = new AdvertiseData.Builder();

        advertiseDataBuilder.setIncludeTxPowerLevel(false);
        advertiseDataBuilder.setIncludeDeviceName(false);
        advertiseDataBuilder.addManufacturerData(MANUFACTURER_ID, adData.array());

        return advertiseDataBuilder.build();
    }

    private static AdvertiseSettings setAdvertiseSettings() {
        AdvertiseSettings.Builder mBuilder = new AdvertiseSettings.Builder();

        mBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);//1000 ms interval
        mBuilder.setConnectable(true);
        mBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM);
        return mBuilder.build();
    }

    public void startAdvertising(ByteBuffer adData) {
        AdvertiseSettings mAdvertiseSettings = setAdvertiseSettings();
        AdvertiseData mAdvertiseData = setAdvertiseData(adData);
        mBluetoothLeAdvertiser.startAdvertising(mAdvertiseSettings, mAdvertiseData, mAdvertiseCallback);
    }

    public void stopAdvertising() {
        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
    }
}

