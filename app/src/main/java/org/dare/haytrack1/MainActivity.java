package org.dare.haytrack1;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.dare.haytrack1.bluetooth.SerialBluetooth;
import org.dare.haytrack1.camera.Camera;
import org.dare.haytrack1.camera.ImagePreviewer;
import org.dare.haytrack1.camera.ImageStreamer;
import org.dare.haytrack1.controller.ServoController;
import org.dare.haytrack1.websocket.Server;

import java.net.UnknownHostException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // by enabling -g in install flags all permissions in manifest are auto granted

    State globallState = new State();

    Button recordButton;

    ServoController controller = new ServoController();

    SerialBluetooth bluetooth;

    Server websocket;


    ImagePreviewer previewer;

    Camera camera;


    ImageView preview;
    private final int PORT_MIN = 8000;
    private final int PORT_MAX = 9000;
    private final int PORT = PORT_MIN + (int)(Math.random() * ((PORT_MAX - PORT_MIN) + 1));

    public void startRecording() {
        camera.startRecording(this);
        recordButton.setText("Stop Recording");
        globallState.isRecording = true;
    }

    public void stopRecording() {
        camera.stopRecording();
        recordButton.setText("Start Recording");
        globallState.isRecording = false;
    }

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
        recordButton = findViewById(R.id.recordButton);
        recordButton.setOnClickListener(v -> {
            if (!globallState.isRecording) {
                startRecording();
            } else {
                stopRecording();
            }
        });

        preview = findViewById(R.id.imageView);

        controller.initializeLogging(this);

        bluetooth = new SerialBluetooth(this, controller);

        startServer();

        previewer = new ImagePreviewer(this, preview);

        camera = new Camera(this, List.of(previewer));


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