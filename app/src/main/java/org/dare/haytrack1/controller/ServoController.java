package org.dare.haytrack1.controller;

import android.util.Log;
import android.os.SystemClock;

import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface;

public class ServoController {
    private final SimpleBluetoothDeviceInterface device;
    private final int COOLDOWN = 20; // milliseconds
    private long lastSentTime = 0;

    public ServoController(SimpleBluetoothDeviceInterface device) {
        this.device = device;
    }

    // position between 0 to 79
    public boolean updatePosition(short horizontal, short vertical) {
        if (!isCooldownOver()) {
            Log.d("ServoController", "Cooldown not finished. Skipping send.");
            return false;
        }

        char[] payload = {encodePosition(horizontal), encodePosition(vertical)};
        device.sendMessage(new String(payload));
        lastSentTime = SystemClock.elapsedRealtime(); // mark time after sending
        return true;
    }

    // resolution of 80
    public static char encodePosition(short value) {
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
