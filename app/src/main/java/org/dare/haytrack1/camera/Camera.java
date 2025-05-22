package org.dare.haytrack1.camera;

import android.content.Context;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Range;

import androidx.annotation.OptIn;
import androidx.camera.camera2.interop.Camera2Interop;
import androidx.camera.camera2.interop.ExperimentalCamera2Interop;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@OptIn(markerClass = ExperimentalCamera2Interop.class)
public class Camera {

    public Camera(Context context, List<ImageAnalyzer> analyzers) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(() -> {
                    try {
                        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                        ImageAnalysis.Builder imageAnalysisBuilder = new ImageAnalysis.Builder();
//                        Camera2Interop.Extender extender = new Camera2Interop.Extender<>(imageAnalysisBuilder); // not all phones support 60fps
//                        extender.setCaptureRequestOption(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, new Range<>(60, 60));

                        ImageAnalysis imageAnalysis = imageAnalysisBuilder
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                                .build();

                        ExecutorService analysisExecutor = Executors.newSingleThreadExecutor();
                        imageAnalysis.setAnalyzer(analysisExecutor, image -> {

                            for (ImageAnalyzer analyzer : analyzers) {
                                analyzer.process(image);
                            }
                            image.close();
                        });

                        cameraProvider.unbindAll();
                        cameraProvider.bindToLifecycle(
                                (androidx.lifecycle.LifecycleOwner) context,
                                cameraSelector,
                                imageAnalysis
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(context));
    }
}
