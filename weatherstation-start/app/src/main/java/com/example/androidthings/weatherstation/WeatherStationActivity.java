/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.androidthings.weatherstation;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.contrib.driver.apa102.Apa102;
import com.google.android.things.contrib.driver.bmx280.Bmx280SensorDriver;
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;

import java.io.IOException;

public class WeatherStationActivity extends Activity {
    private static final String TAG = WeatherStationActivity.class.getSimpleName();

    // Default LED brightness
    private static final int LEDSTRIP_BRIGHTNESS = 1;

    // Peripheral drivers
    private Bmx280SensorDriver mEnvironmentalSensorDriver;
    private AlphanumericDisplay mDisplay;
    private Apa102 mLedstrip;

    private SensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSensorManager = getSystemService(SensorManager.class);

        // Initialize temperature/pressure sensors
        try {
            mEnvironmentalSensorDriver = new Bmx280SensorDriver(BoardDefaults.getI2cBus());
            Log.d(TAG, "Initialized I2C BMP280");
        } catch (IOException e) {
            throw new RuntimeException("Error initializing BMP280", e);
        }

        // Initialize 7-segment display
        try {
            mDisplay = new AlphanumericDisplay(BoardDefaults.getI2cBus());
            mDisplay.setEnabled(true);
            mDisplay.clear();
            Log.d(TAG, "Initialized I2C Display");
        } catch (IOException e) {
            throw new RuntimeException("Error initializing display", e);
        }

        // Initialize LED strip
        try {
            mLedstrip = new Apa102(BoardDefaults.getSpiBus(), Apa102.Mode.BGR);
            mLedstrip.setBrightness(LEDSTRIP_BRIGHTNESS);
            Log.d(TAG, "Initialized SPI LED strip");
        } catch (IOException e) {
            throw new RuntimeException("Error initializing LED strip", e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mSensorManager.registerDynamicSensorCallback(mDynamicSensorCallback);
        mEnvironmentalSensorDriver.registerTemperatureSensor();
        mEnvironmentalSensorDriver.registerPressureSensor();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mSensorManager.unregisterListener(mSensorEventListener);
        mSensorManager.unregisterDynamicSensorCallback(mDynamicSensorCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Close peripheral connections
        if (mEnvironmentalSensorDriver != null) {
            try {
                mEnvironmentalSensorDriver.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing sensors", e);
            } finally {
                mEnvironmentalSensorDriver = null;
            }
        }

        if (mDisplay != null) {
            try {
                mDisplay.clear();
                mDisplay.setEnabled(false);
                mDisplay.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing display", e);
            } finally {
                mDisplay = null;
            }
        }

        if (mLedstrip != null) {
            try {
                mLedstrip.write(new int[7]);
                mLedstrip.setBrightness(0);
                mLedstrip.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing LED strip", e);
            } finally {
                mLedstrip = null;
            }
        }
    }

    /**
     * Update the 7-segment display with the latest temperature value.
     * @param event Latest sensor event.
     */
    private void updateSensorDisplay(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_AMBIENT_TEMPERATURE) {
            return;
        }

        final float value = event.values[0];
        if (mDisplay != null) {
            try {
                mDisplay.display(value);
            } catch (IOException e) {
                Log.e(TAG, "Error updating display", e);
            }
        }
    }

    /**
     * Update LED strip based on the latest pressure value.
     * @param event Latest sensor event.
     */
    private void updateBarometerDisplay(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_PRESSURE) {
            return;
        }

        final float pressure = event.values[0];

        // Update led strip.
        if (mLedstrip != null) {
            try {
                int[] colors = RainbowUtil.getWeatherStripColors(pressure);
                mLedstrip.write(colors);
            } catch (IOException e) {
                Log.e(TAG, "Error updating ledstrip", e);
            }
        }
    }

    // Callback used when we register the BMP280 sensor driver with the system's SensorManager.
    private SensorManager.DynamicSensorCallback mDynamicSensorCallback =
            new SensorManager.DynamicSensorCallback() {
                @Override
                public void onDynamicSensorConnected(Sensor sensor) {
                    if (sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE
                            || sensor.getType() == Sensor.TYPE_PRESSURE) {
                        // Our sensor is connected. Start receiving data.
                        mSensorManager.registerListener(mSensorEventListener, sensor,
                                SensorManager.SENSOR_DELAY_NORMAL);
                    }
                }

                @Override
                public void onDynamicSensorDisconnected(Sensor sensor) {
                    super.onDynamicSensorDisconnected(sensor);
                }
            };

    // Callback when SensorManager delivers new data.
    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            updateSensorDisplay(event);
            updateBarometerDisplay(event);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d(TAG, "accuracy changed: " + accuracy);
        }
    };
}
