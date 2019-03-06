package com.sonymobile.ibeaconservice;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;

import com.sonymobile.ibeaconservice.Model.AdData;
import com.sonymobile.ibeaconservice.Service.IBeaconService;
import com.sonymobile.ibeaconservice.Utils.Constants;
import com.sonymobile.ibeaconservice.Utils.IBeaconUtils;

import java.nio.ByteBuffer;
import java.util.Objects;

import static com.sonymobile.ibeaconservice.Utils.Constants.Intents.SERVICE_OPEN_APP_INTENT;

public class MainActivity extends AppCompatActivity {
    private Button mStartButton;
    private Button mStopButton;
    private Button mSettingsButton;
    private Button mQuitButton;
    private ByteBuffer mAdvertisingBytes;

    static final String AD_DATA_KEY = "advertising_data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mStartButton = findViewById(R.id.start_button);
        mStopButton = findViewById(R.id.stop_button);
        mSettingsButton = findViewById(R.id.settings_button);
        mQuitButton = findViewById(R.id.quit);

        if (intent != null) {
            //app opened from service's notification bar
            if (Objects.equals(intent.getAction(), SERVICE_OPEN_APP_INTENT)) {
                mStartButton.setEnabled(false);

                if(intent.getExtras() != null) {
                    mAdvertisingBytes = ByteBuffer.wrap(intent.getExtras()
                            .getByteArray(Constants.Extras.ADVERTISING_BYTES));
                }
            } else {//normal start. Check if service is running..
                byte[] adBytes = retrieveBytesFromSharedPreferences();

                if (adBytes.length != 0) {
                   mAdvertisingBytes = ByteBuffer.wrap(adBytes);
                } else {
                    mAdvertisingBytes = IBeaconUtils.createDefaultIBeaconAdvertisement();
                }
                if ( !((IBeaconApplication)getApplication()).mRunning ){
                    mStopButton.setEnabled(false);
                } else {
                    mStartButton.setEnabled(false);
                }
            }
        }

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleStartButtonClicked();
            }
        });

        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleStopButtonClicked();
            }
        });

        mSettingsButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               handleSettingsButtonClicked();
           }
        });

        mQuitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /**
     * Called when SettingsActivity has finished. Contains the new advertising data that the
     * user may have specified.
     * @param requestCode identifies the activity called in StartActiviyForResult
     * @param resultCode success/failure etc. UNUSED
     * @param data intent containing in our case an AdData object with the values set in the
     *             settings activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case Constants.ActivityRequestCodes.SETTINGS_ACTIVITY_REQUEST_CODE:
                if (data != null) {
                    AdData ad = data.getParcelableExtra("result");

                    mAdvertisingBytes = ByteBuffer.wrap(ad.getManufacturerDataBytes());

                    //save data to shared prefs getting preferences from a specified file
                    saveBytesToSharedPreferences(ad.getManufacturerDataBytes());

                    //stop advertising and start again with new data
                    handleStopButtonClicked();
                    handleStartButtonClicked();
                }
                break;
                default:
                    System.out.println("towa on activity result called by unknown activity. Which is odd");
                    break;
        }
    }

    void startIBeaconService(byte[] advertisingDataBytes) {
        Intent intent = new Intent(getApplicationContext(), IBeaconService.class);
        intent.setAction(Constants.Intents.SERVICE_INTENT_FOREGROUND);
        intent.putExtra(Constants.Extras.ADVERTISING_BYTES,advertisingDataBytes);
        startService(intent);
    }

    void handleStopButtonClicked() {
        mStopButton.setEnabled(false);
        mStartButton.setEnabled(true);
        Intent intent = new Intent(getApplicationContext(), IBeaconService.class);
        stopService(intent);
    }

    void handleStartButtonClicked() {
        mStartButton.setEnabled(false);
        startIBeaconService(mAdvertisingBytes.array());
        mStopButton.setEnabled(true);
        finish();
    }

    void handleSettingsButtonClicked() {
        if (mAdvertisingBytes != null) {
            Intent advertiseIntent = new Intent(getApplicationContext(), SettingsActivity.class);
            advertiseIntent.putExtra(Constants.Extras.ADVERTISING_BYTES, mAdvertisingBytes.array());
            startActivityForResult(advertiseIntent, Constants.ActivityRequestCodes.SETTINGS_ACTIVITY_REQUEST_CODE);
        }
    }

    void saveBytesToSharedPreferences(byte[] adDataBytes) {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = preferences.edit();
        String adDataString = Base64.encodeToString(adDataBytes, Base64.DEFAULT);

        edit.putString(AD_DATA_KEY, adDataString);
        edit.apply();
    }

    byte[] retrieveBytesFromSharedPreferences(){
        String adDataString;
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);

        adDataString = preferences.getString(AD_DATA_KEY,"");

        if (adDataString != null) {
            return Base64.decode(adDataString, 0);
        } else {
            return null;
        }
    }
}
