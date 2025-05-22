package org.dare.haytrack1;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.dare.haytrack1.bluetooth.SerialBluetooth;
import org.dare.haytrack1.camera.Camera;
import org.dare.haytrack1.camera.ImageStreamer;
import org.dare.haytrack1.controller.ServoController;
import org.dare.haytrack1.websocket.Server;

import java.net.UnknownHostException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // by enabling -g in install flags all permissions in manifest are auto granted

    State globallState = new State();

    ServoController controller = new ServoController();
    SerialBluetooth bluetooth;

    Server websocket;

    ImageStreamer streamer;

    Camera camera;
    private final int PORT = 8887;

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
        bluetooth = new SerialBluetooth(this, controller);

        streamer = new ImageStreamer(websocket, globallState);

        camera = new Camera(this, List.of(streamer));
    }

    public void setIpText(String address, int port) {
        TextView addressText = findViewById(R.id.ipAddress);
        String uri = address + ":" + String.valueOf(port);
        addressText.setText(uri);
    }

    public void startServer() {
        try {
            websocket = new Server(PORT, this, controller, globallState);
            websocket.start();




        } catch (UnknownHostException e) {
            Log.e("MainActivity", e.getMessage());
            Toast.makeText(this, "Error starting websocket server", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (websocket != null) {
            try {
                websocket.stop(0);
            } catch (InterruptedException e) {
                Log.e("MainActivity", e.getMessage());
            }
        }
    }
}