package com.sonymobile.ibeaconservice.Model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Class representing the advertising data that we will send.
 * The data we can actually explicitly set is only 21 bytes long
 * (ProximityUUID battery, major byte minor byte, signal power)
 */

public class AdData implements Parcelable {
    private static final int UUID_BYTES_INDEX = 2;
    private static final int BATTERY_VOLTAGE_INDEX = 16;
    private static final int MAJOR_BYTE_INDEX = 18;
    private static final int MINOR_BYTE_INDEX = 20;
    private static final int SIGNAL_POWER_BYTES_INDEX = 22;

    public static final int UUID_DATA_LENGTH = 14;
    public static final int BATTERY_VOLTAGE_DATA_LENGTH = 2;
    public static final int MAJOR_BYTES_LENGTH = 2;
    public static final int MINOR_BYTES_LENGTH = 2;
    private static final int SIGNAL_POWER_BYTES_LENGTH = 1;

    private byte[] mUUIDbytes;
    private byte[] mBatteryVoltage;
    private byte[] mMajorBytes;
    private byte[] mMinorBytes;
    private byte[] mSignalPowerBytes;

    private final String TAG = AdData.class.getName();

    public AdData() {

    }

    /**
     * expect 23 bytes of data here. We want to throw away the first 2 bytes (iBeaconId and
     * data length) as they are always the same
     *
     * @param adData 23 bytes of ad data.
     */
    public AdData(byte[] adData) {
        mUUIDbytes = new byte[UUID_DATA_LENGTH];
        mBatteryVoltage = new byte[BATTERY_VOLTAGE_DATA_LENGTH];
        mMajorBytes = new byte[MAJOR_BYTES_LENGTH];
        mMinorBytes = new byte[MINOR_BYTES_LENGTH];
        mSignalPowerBytes = new byte[SIGNAL_POWER_BYTES_LENGTH];

        System.arraycopy(adData, UUID_BYTES_INDEX, mUUIDbytes, 0, UUID_DATA_LENGTH);
        System.arraycopy(adData,BATTERY_VOLTAGE_INDEX, mBatteryVoltage, 0, BATTERY_VOLTAGE_DATA_LENGTH);
        System.arraycopy(adData,MAJOR_BYTE_INDEX, mMajorBytes, 0, MAJOR_BYTES_LENGTH);
        System.arraycopy(adData,MINOR_BYTE_INDEX, mMinorBytes, 0, MINOR_BYTES_LENGTH);
        System.arraycopy(adData,SIGNAL_POWER_BYTES_INDEX, mSignalPowerBytes,0, SIGNAL_POWER_BYTES_LENGTH);
    }

    private static String convertValuesToHexString(byte[] bytes) {
        StringBuilder builder = new StringBuilder();

        for (byte aByte : bytes) {
            builder.append(String.format("%02x", aByte));
        }

        return builder.toString();
    }

    private static byte[] convertStringToBytes(String string){
        int len = string.length();
        byte[] data = new byte[len/2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(string.charAt(i), 16) << 4)
                    + Character.digit(string.charAt(i+1), 16));
        }
        return data;
    }

    public String getUUIDString() {
        return convertValuesToHexString(mUUIDbytes);
    }

    public String getBatteryVoltage() {
        return convertValuesToHexString(mBatteryVoltage);
    }

    public String getMajorBytesString() {
        return convertValuesToHexString(mMajorBytes);
    }

    public String getMinorBytesString() {
        return convertValuesToHexString(mMinorBytes);
    }

    public String getSignalPowerBytesString() {
        return convertValuesToHexString(mSignalPowerBytes);
    }

    public void setUUIDBytes(String proximityString) {
        mUUIDbytes = convertStringToBytes(proximityString).clone();
    }

    public void setBatteryVoltageBytes(String batteryVoltage) {
        mBatteryVoltage = convertStringToBytes(batteryVoltage).clone();
    }

    public void setMajorBytes(String majorBytes) {
        mMajorBytes = convertStringToBytes(majorBytes).clone();
    }

    public void setMinorBytes(String minorBytes) {
        mMinorBytes = convertStringToBytes(minorBytes).clone();
    }

    public void setSignalPowerBytes(String signalPowerBytes) {
        mSignalPowerBytes = convertStringToBytes(signalPowerBytes).clone();
    }

    /**
     * return the editable parts of the manufacturing data
     *
     * @return 21 bytes of manufacturing data corresponding to the
     * members of this data type
     */
    private byte[] getAdDataBytes() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            outputStream.write(this.mUUIDbytes);//14
            outputStream.write(this.mBatteryVoltage);//2
            outputStream.write(this.mMajorBytes);//2
            outputStream.write(this.mMinorBytes);//2
            outputStream.write(this.mSignalPowerBytes);//1
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }

    /**
     * get the editable advertising bytes, add the header
     * with Manufacturer data and length and the TX power footer
     *
     * @return 23 bytes (Header + Ad bytes + footer)
     */
    public byte[] getManufacturerDataBytes() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] headerBytes = {(byte)0x02,(byte)0x15};

        try {
            outputStream.write(headerBytes);
            outputStream.write(getAdDataBytes());
        } catch (IOException e) {
            Log.d(TAG,"Exception writing manufacturing data bytes to output stream");
        }
        return outputStream.toByteArray();
    }


    /*
    Parcelable functionality.
     */
    @Override
    public int describeContents() {
        return hashCode();
    }

    /**
     *Create an AdData object from a parcel. Called by Creator function below
     * @param in parcel to construct AdData from
     */
    private AdData(Parcel in) {
        mUUIDbytes = in.createByteArray();
        mBatteryVoltage = in.createByteArray();
        mMajorBytes = in.createByteArray();
        mMinorBytes = in.createByteArray();
        mSignalPowerBytes = in.createByteArray();
    }

    /**
     *
     */
    public static final Creator<AdData> CREATOR = new Creator<AdData>() {
        @Override
        public AdData createFromParcel(Parcel in) {
            return new AdData(in);
        }

        @Override
        public AdData[] newArray(int size) {
            return new AdData[size];
        }
    };

    /**
     * Parcelable functionality for AdData object to allow us to send it as intent data.
     * One alternative would be to use getAdDataBytes to serialise it into a byte array as
     * this does more or less the same thing given that the object just consists of a load
     * of byte arrays. The functionaliy is more suited to an object with lots of different
     * data types.
     * This was done as a learning exercise.
     *
     * @param parcel the parcel to be written to
     * @param i flags. Not used
     */
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByteArray(mUUIDbytes);
        parcel.writeByteArray(mBatteryVoltage);
        parcel.writeByteArray(mMajorBytes);
        parcel.writeByteArray(mMinorBytes);
        parcel.writeByteArray(mSignalPowerBytes);
    }
}

