package org.dare.haytrack1.camera;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.camera.core.ImageProxy;

import org.dare.haytrack1.State;
import org.dare.haytrack1.websocket.Server;
import java.io.ByteArrayOutputStream;


public class ImageStreamer extends ImageAnalyzer{
    private final Server server;
    private State globalState;
    public ImageStreamer(Server server, State globalState) {
        this.server = server;
        this.globalState = globalState;
    }
    @Override
    public void process(ImageProxy img) {
        if (!globalState.isStreaming) return;
        Bitmap bitmap = rotateBitmap(rgbaMatToBitmap(imageProxyToRgbaMat(img)), ROTATION_CORRECTION);
        server.broadcast(bitmapToJpeg(bitmap));
        bitmap.recycle();
    }

    private byte[] bitmapToJpeg(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, stream); // 80 is the quality
        return stream.toByteArray();
    }
}
