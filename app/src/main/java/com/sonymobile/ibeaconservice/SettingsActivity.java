package com.sonymobile.ibeaconservice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sonymobile.ibeaconservice.Model.AdData;
import com.sonymobile.ibeaconservice.Utils.Constants;

import java.nio.ByteBuffer;


public class SettingsActivity extends Activity {
    ByteBuffer mAdvertisingBytes;
    Button mAdvertButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            byte[] intentData = getIntent().getExtras().getByteArray(Constants.Extras.ADVERTISING_BYTES);
            assert intentData != null;
            mAdvertisingBytes = ByteBuffer.wrap(intentData.clone());
        } else {
            finish();
        }

        setContentView(R.layout.settings_activity);
        mAdvertButton = findViewById(R.id.doneButton);

        mAdvertButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                byte validity = checkDataValidity();

                if (validity == 0x0f) {
                    Intent returnIntent = new Intent();
                    AdData adData = extractDataFromForm();

                    returnIntent.putExtra("result", adData);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                } else {
                    String message = getResources().getString(R.string.not_enough_data_txt);

                    if ((validity & 0x01) == 0) {
                        message += getResources().getString(R.string.UUID_txt);
                    }
                    if ((validity & 0x02) == 0) {
                        message += getResources().getString(R.string.voltage_txt);
                    }
                    if ((validity & 0x04) == 0) {
                        message += getResources().getString(R.string.major_byte_txt);
                    }
                    if ((validity & 0x08) == 0) {
                        message += getResources().getString(R.string.minor_byte_txt);
                    }
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                }
            }
        });
        populateDataForm();
    }

    /**
     *  mAdvertising bytes is 23 bytes long
     *
     */
    void populateDataForm(){
        EditText editUUID = findViewById(R.id.editUUID);
        EditText editBatteryVoltage = findViewById(R.id.editBatteryVoltage);
        EditText editMajor = findViewById(R.id.editMajor);
        EditText editMinor = findViewById(R.id.editMinor);
        EditText editSignalPower = findViewById(R.id.editSignalPower);

        AdData adData = new AdData(mAdvertisingBytes.array());

        editUUID.setText(adData.getUUIDString());
        editBatteryVoltage.setText(adData.getBatteryVoltage());
        editMajor.setText(adData.getMajorBytesString());
        editMinor.setText(adData.getMinorBytesString());
        editSignalPower.setText(adData.getSignalPowerBytesString());
    }

    private AdData extractDataFromForm() {
        AdData adData = new AdData();

        EditText editUUID = findViewById(R.id.editUUID);
        EditText editBatteryVoltage = findViewById(R.id.editBatteryVoltage);
        EditText editMajor = findViewById(R.id.editMajor);
        EditText editMinor = findViewById(R.id.editMinor);
        EditText editSignalPower = findViewById(R.id.editSignalPower);

        adData.setUUIDBytes(editUUID.getText().toString());
        adData.setBatteryVoltageBytes(editBatteryVoltage.getText().toString());
        adData.setMajorBytes(editMajor.getText().toString());
        adData.setMinorBytes(editMinor.getText().toString());
        adData.setSignalPowerBytes(editSignalPower.getText().toString());

        return adData;
    }

    /**
     * Fetch form data, check there is enough of it.
     *
     * @return flags showing which parameters are valid
     */
    byte checkDataValidity() {
        byte result = (byte)0x00;
        EditText editUUID = findViewById(R.id.editUUID);
        EditText editBatteryVoltage = findViewById(R.id.editBatteryVoltage);
        EditText editMajor = findViewById(R.id.editMajor);
        EditText editMinor = findViewById(R.id.editMinor);

        if (editUUID.getText().length() == AdData.UUID_DATA_LENGTH * 2) {
            result |= 1;
        }
        if (editBatteryVoltage.getText().length() == AdData.BATTERY_VOLTAGE_DATA_LENGTH * 2) {
            result |= 1 << 1;
        }
        if (editMajor.getText().length() == AdData.MAJOR_BYTES_LENGTH * 2) {
            result |= 1 << 2;
        }
        if (editMinor.getText().length() == AdData.MINOR_BYTES_LENGTH * 2) {
            result |= 1 << 3;
        }
        return result;
    }
}
