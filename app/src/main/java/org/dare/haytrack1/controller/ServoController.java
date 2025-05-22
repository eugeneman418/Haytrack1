package org.dare.haytrack1.controller;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServoController {
    private SimpleBluetoothDeviceInterface device;
    private final int COOLDOWN = 20; // milliseconds
    private long lastSentTime = 0;

    private int horizontal_pos = 40;
    private int vertical_pos = 40;

    private boolean isLoggingEnabled = false;
    private FileWriter logWriter;
    private Context context;  // store context for logging path

    public void setDevice(SimpleBluetoothDeviceInterface device) {
        this.device = device;
    }

    // Should be called once before logging
    public void initializeLogging(Context context) {
        this.context = context.getApplicationContext(); // retain safe reference
    }

    public void startLogging() {
        if (context == null) {
            Log.e("ServoController", "Logging not initialized. Call initializeLogging() first.");
            return;
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        File logFile = new File(context.getExternalFilesDir(null), "log_" + timestamp + ".csv");

        try {
            logWriter = new FileWriter(logFile, true);
            logWriter.append("timestamp,horizontal,vertical\n");
            isLoggingEnabled = true;
            Log.d("ServoController", "Logging started to: " + logFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e("ServoController", "Failed to start logging", e);
        }
    }

    public void stopLogging() {
        if (!isLoggingEnabled) return;

        try {
            if (logWriter != null) {
                logWriter.flush();
                logWriter.close();
                Log.d("ServoController", "Logging stopped and file saved.");
            }
        } catch (IOException e) {
            Log.e("ServoController", "Error closing log file", e);
        } finally {
            logWriter = null;
            isLoggingEnabled = false;
        }
    }

    public boolean overwritePosition(int horizontal, int vertical) {
        Log.d("ServoController", "horizontal: " + horizontal + " vertical: " + vertical);
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
        lastSentTime = SystemClock.elapsedRealtime();

        if (isLoggingEnabled) {
            logPosition();
        }

        return true;
    }

    public boolean move(int horizontal_displacement, int vertical_displacement) {
        int horizontal = clamp(horizontal_pos + horizontal_displacement, 0, 79);
        int vertical = clamp(vertical_pos + vertical_displacement, 0, 79);
        return overwritePosition(horizontal, vertical);
    }

    private static int clamp(int value, int low, int high) {
        return Math.max(low, Math.min(high, value));
    }

    public static char encodePosition(int value) {
        if (value < 0 || value > 79) {
            throw new IllegalArgumentException("Value must be between 0 and 79.");
        }
        return (char) (0x20 + value);
    }

    public boolean isCooldownOver() {
        return SystemClock.elapsedRealtime() - lastSentTime >= COOLDOWN;
    }

    private void logPosition() {
        if (logWriter == null) {
            Log.w("ServoController", "LogWriter is null. Skipping log.");
            return;
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        try {
            logWriter.append(String.format("%s,%d,%d\n", timestamp, horizontal_pos, vertical_pos));
            Log.d("ServoController", "Added to entry to log");
        } catch (IOException e) {
            Log.e("ServoController", "Failed to log position", e);
        }
    }
}
