package org.dare.haytrack1;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.dare.haytrack1.bluetooth.SerialBluetooth;
import org.dare.haytrack1.controller.ServoController;
import org.dare.haytrack1.websocket.Server;

import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {
    // by enabling -g in install flags all permissions in manifest are auto granted

    ServoController controller;
    SerialBluetooth bluetooth;

    Server websocket;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        startServer();

        //bluetooth = new SerialBluetooth(this, controller);
    }

    public void startServer() {
        try {
            websocket = new Server(8887);
            websocket.start();
            Toast.makeText(this, Server.getLocalIpAddress(), Toast.LENGTH_LONG).show();

        } catch (UnknownHostException e) {
            Log.e("MainActivity", e.getMessage());
            Toast.makeText(this, "Error starting websocket server", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}