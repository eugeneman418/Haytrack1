package org.dare.haytrack1.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageProxy;

public class ImagePreviewer extends ImageAnalyzer{

    protected float ROTATION_CORRECTION = 270;

    private ImageView view;
    private AppCompatActivity context;
    public ImagePreviewer(AppCompatActivity context, ImageView view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void process(ImageProxy img) {
        Bitmap bitmap = rotateBitmap(rgbaMatToBitmap(imageProxyToRgbaMat(img)), ROTATION_CORRECTION);

        if (bitmap != null) {
            // Update the ImageView on the main thread
            context.runOnUiThread(() ->
                view.setImageBitmap(bitmap)
            );
        }

        //bitmap.recycle();

    }
}
