package org.dare.haytrack1.websocket;

import android.util.Log;

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

    private final short MOVE_UP = 1;
    private final short MOVE_DOWN = 2;
    private final short MOVE_LEFT = 3;
    private final short MOVE_RIGHT = 4;



    public Server(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conn.send("Hello World");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {

    }

    @Override
    public void onMessage(WebSocket conn, String message) {

    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        try (MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(message);) {
            short instruction = unpacker.unpackShort();
            short value = unpacker.unpackShort();
            Log.d("Server", "Instruction: " + instruction +", value: "+value);
        } catch (IOException e) {
            Log.e("Server", e.getMessage());
        }


    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

    }

    @Override
    public void onStart() {
        Log.d("Server", "Server started!");
        setConnectionLostTimeout(500);
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
