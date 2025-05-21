package org.dare.haytrack1.camera;

import android.graphics.Bitmap;

import androidx.camera.core.ImageProxy;

import org.dare.haytrack1.websocket.Server;
import java.io.ByteArrayOutputStream;


public class ImageStreamer extends ImageAnalyzer{
    private final Server server;
    public ImageStreamer(Server server) {
        this.server = server;
    }
    @Override
    public void process(ImageProxy img) {
        Bitmap bitmap = rotateBitmap(rgbaMatToBitmap(imageProxyToRgbaMat(img)), ROTATION_CORRECTION);
        server.broadcast(bitmapToJpeg(bitmap));
        bitmap.recycle();
    }

    private byte[] bitmapToJpeg(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream); // 80 is the quality
        return stream.toByteArray();
    }
}
