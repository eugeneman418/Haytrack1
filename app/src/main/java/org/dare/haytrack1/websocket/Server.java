package org.dare.haytrack1.websocket;

import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.dare.haytrack1.MainActivity;
import org.dare.haytrack1.R;
import org.dare.haytrack1.State;
import org.dare.haytrack1.controller.ServoController;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Enumeration;

public class Server extends WebSocketServer {

    private final String START_STREAMING = "START_STREAMING";
    private final String STOP_STREAMING = "STOP_STREAMING";

    private final String START_RECORDING = "START_RECORDING";
    private final String STOP_RECORDING = "STOP_RECORDING";

    private final String START_MANUAL = "MANUAL_CONTROL";
    private final String STOP_MANUAL = "AI_TAKE_THE_WHEEL";

    private ServoController servoController;
    MainActivity context;
    State globalState;

    public Server(int port, MainActivity context, ServoController servoController, State globalState) throws UnknownHostException {
        super(new InetSocketAddress(port));
        this.context = context;
        this.servoController = servoController;
        this.globalState = globalState;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        // Optional: Handle disconnection cleanup here
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        context.runOnUiThread(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());

        // Optional: handle string-based commands like START/STOP here
        switch (message) {
            case START_STREAMING:
                globalState.isStreaming = true;
                Log.d("WebSocket", "Start manual control received");
                break;
            case STOP_STREAMING:
                globalState.isStreaming = false;
                Log.d("WebSocket", "Stop manual control received");
                break;
            case START_RECORDING:
                context.startRecording();
                Log.d("WebSocket", "Start recording received");
                break;
            case STOP_RECORDING:
                context.stopRecording();
                Log.d("WebSocket", "Stop recording received");
                break;
            default:
                Log.d("WebSocket", "Unknown string command: " + message);
                break;
        }
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        try {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(message);
            int length = unpacker.unpackArrayHeader();
            if (length == 2) {
                int move_horizontal = unpacker.unpackInt();
                int move_vertical = unpacker.unpackInt();
                servoController.move(move_horizontal, move_vertical);
            } else {
                Log.w("WebSocket", "Unexpected msgpack array length: " + length);
            }
        } catch (IOException e) {
            context.runOnUiThread(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Log.e("Server", "Error: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        context.runOnUiThread(() ->
                context.setIpText(getLocalIpAddress(), getPort())
        );

        setConnectionLostTimeout(100);
        setReuseAddr(true);
    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("IP Address", ex.toString());
        }
        return null;
    }
}
