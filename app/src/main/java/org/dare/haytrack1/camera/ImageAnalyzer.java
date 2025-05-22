package org.dare.haytrack1.camera;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import androidx.camera.core.ImageProxy;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;

abstract class ImageAnalyzer {
    static {
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Unable to load OpenCV");
        } else {
            Log.d("OpenCV", "OpenCV loaded successfully");
        }
    }
    abstract public void process(ImageProxy img);

    protected float ROTATION_CORRECTION = 90;

    protected Mat imageProxyToRgbaMat(ImageProxy imageProxy) {
        // Get the image buffer from the first plane
        ByteBuffer buffer = imageProxy.getPlanes()[0].getBuffer();
        buffer.rewind();

        // Create a byte array to hold pixel data
        byte[] rgbaBytes = new byte[buffer.remaining()];
        buffer.get(rgbaBytes);

        // Get image dimensions
        int width = imageProxy.getWidth();
        int height = imageProxy.getHeight();

        // Create a Mat of appropriate size and type (RGBA => 4 channels, 8-bit)
        Mat rgbaMat = new Mat(height, width, CvType.CV_8UC4);

        // Fill the Mat with byte data
        rgbaMat.put(0, 0, rgbaBytes);

        return rgbaMat;
    }

    protected Bitmap rgbaMatToBitmap(Mat rgba) {
        Bitmap bitmap = Bitmap.createBitmap(rgba.cols(), rgba.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgba, bitmap);
        return bitmap;
    }




    // Converts RGB Mat to grayscale Mat
    protected Mat rgbToGrayscale(Mat rgb) {
        Mat gray = new Mat();
        Imgproc.cvtColor(rgb, gray, Imgproc.COLOR_RGB2GRAY);
        return gray;
    }

    // Rotates Bitmap
    protected Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}
