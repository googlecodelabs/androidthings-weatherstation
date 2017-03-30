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
import android.os.Bundle;
import android.util.Log;

public class WeatherStationActivity extends Activity {
    private static final String TAG = WeatherStationActivity.class.getSimpleName();

    // Default LED brightness
    private static final int LEDSTRIP_BRIGHTNESS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Weather Station Started");

        //TODO: Register peripheral drivers here
    }

    @Override
    protected void onStart() {
        super.onStart();

        //TODO: Register for sensor events here
    }

    @Override
    protected void onStop() {
        super.onStop();

        //TODO: Unregister for sensor events here
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //TODO: Close peripheral connections here
    }

    /**
     * Update the 7-segment display with the latest temperature value.
     *
     * @param temperature Latest temperature value.
     */
    private void updateTemperatureDisplay(float temperature) {
        //TODO: Add code to write a value to the segment display
    }

    /**
     * Update LED strip based on the latest pressure value.
     *
     * @param pressure Latest pressure value.
     */
    private void updateBarometerDisplay(float pressure) {
        //TODO: Add code to send color data to the LED strip
    }

}
