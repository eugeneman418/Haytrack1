package org.dare.haytrack1.controller;

import android.util.Log;
import android.os.SystemClock;

import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface;

public class ServoController {
    private SimpleBluetoothDeviceInterface device;
    private final int COOLDOWN = 20; // milliseconds
    private long lastSentTime = 0;

    private int horizontal_pos = 40;
    private int vertical_pos = 40;

    public void setDevice(SimpleBluetoothDeviceInterface device) {
        this.device = device;
    }

    // Position between 0 to 79
    public boolean overwritePosition(int horizontal, int vertical) {
        if (device == null) {
            Log.e("ServoController", "Bluetooth not connected. Skipping send.");
            return false;
        } else if (!isCooldownOver()) {
            Log.d("ServoController", "Cooldown not finished. Skipping send.");
            return false;
        }

        horizontal_pos = horizontal;
        vertical_pos = vertical;
        char[] payload = {encodePosition(horizontal_pos), encodePosition(vertical_pos)};
        device.sendMessage(new String(payload));
        lastSentTime = SystemClock.elapsedRealtime(); // mark time after sending
        return true;
    }

    public boolean move(int horizontal_displacement, int vertical_displacement) {
        int horizontal = clamp(horizontal_pos + horizontal_displacement, 0, 79);
        int vertical = clamp(vertical_pos + vertical_displacement, 0, 79);
        return overwritePosition(horizontal, vertical);
    }

    private static int clamp(int value, int low, int high) {
        value = value < low ? low : value;
        value = value > high ? high : value;
        return value;
    }

    // Resolution of 80
    public static char encodePosition(int value) {
        if (value < 0 || value > 79) {
            throw new IllegalArgumentException("Value must be between 0 and 79.");
        }
        return (char) (0x20 + value);
    }

    // Public method to check cooldown status
    public boolean isCooldownOver() {
        return SystemClock.elapsedRealtime() - lastSentTime >= COOLDOWN;
    }
}
