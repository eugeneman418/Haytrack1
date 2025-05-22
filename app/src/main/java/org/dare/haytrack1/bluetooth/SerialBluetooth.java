package org.dare.haytrack1.bluetooth;
import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;

import com.harrysoft.androidbluetoothserial.BluetoothManager;
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice;
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface;

import org.dare.haytrack1.controller.ServoController;

import java.lang.annotation.Target;
import java.util.Collection;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SerialBluetooth {
    private final String DEVICE_NAME = "Haytrack";
    BluetoothManager deviceManager;

    AppCompatActivity context;

    ServoController controller;

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public SerialBluetooth(AppCompatActivity context, ServoController controller) {
        this.context = context;
        this.controller = controller;
        deviceManager = BluetoothManager.getInstance();
        if (deviceManager == null) {
            // Bluetooth unavailable on this device :( tell the user
            Toast.makeText(context, "Bluetooth not available.", Toast.LENGTH_LONG).show(); // Replace context with your context instance.
            context.finish();
        }

        BluetoothDevice targetDevice = null;
        Collection<BluetoothDevice> pairedDevices = deviceManager.getPairedDevicesList();
        for (BluetoothDevice device : pairedDevices) {
            if (DEVICE_NAME.equals(device.getName())) {
                targetDevice = device;
                break;
            }
        }
        if (targetDevice == null) {
            Toast.makeText(context, "Target device "+ DEVICE_NAME +" not paired.", Toast.LENGTH_LONG).show(); // Replace context with your context instance.
            context.finish();
        }
        else
            connectDevice(targetDevice.getAddress());

    }

    private void connectDevice(String mac) {
        deviceManager.openSerialDevice(mac)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onConnected, this::onError);
    }

    private void onConnected(BluetoothSerialDevice connectedDevice) {
        // You are now connected to this device!
        // Here you may want to retain an instance to your device:

        // Listen to bluetooth events

        Toast.makeText(context, DEVICE_NAME + " connected.", Toast.LENGTH_SHORT).show();

        controller.setDevice(connectedDevice.toSimpleDeviceInterface());
    }

    private void onError(Throwable error) {
        Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
        context.finish();
    }



}
