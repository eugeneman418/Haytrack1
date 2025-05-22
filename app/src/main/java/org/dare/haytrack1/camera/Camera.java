package org.dare.haytrack1.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.hardware.camera2.CaptureRequest;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Range;

import androidx.annotation.OptIn;
import androidx.annotation.RequiresPermission;
import androidx.camera.camera2.interop.Camera2Interop;
import androidx.camera.camera2.interop.ExperimentalCamera2Interop;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.FileOutputOptions;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@OptIn(markerClass = ExperimentalCamera2Interop.class)
public class Camera {
    private final String SAVE_PATH = "/HayTrack";

    private VideoCapture<Recorder> videoCapture;
    private Recording currentRecording;

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

                        Recorder recorder = new Recorder.Builder()
                                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                                .build();
                        videoCapture = VideoCapture.withOutput(recorder);

                        cameraProvider.unbindAll();
                        cameraProvider.bindToLifecycle(
                                (androidx.lifecycle.LifecycleOwner) context,
                                cameraSelector,
                                imageAnalysis,
                                videoCapture
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

    @SuppressLint("MissingPermission")
    public void startRecording(Context context) {
        if (videoCapture == null) return;

        ContentValues contentValues = new ContentValues();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "recording_" + timestamp);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + SAVE_PATH);

        MediaStoreOutputOptions mediaStoreOutputOptions = new MediaStoreOutputOptions.Builder(
                context.getContentResolver(),
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )
                .setContentValues(contentValues)
                .build();

//        File outputFile = new File(context.getExternalFilesDir(null), "video_" + System.currentTimeMillis() + ".mp4");
//        FileOutputOptions options = new FileOutputOptions.Builder(outputFile).build();

        currentRecording = videoCapture.getOutput()
                .prepareRecording(context, mediaStoreOutputOptions)
                .start(ContextCompat.getMainExecutor(context), videoRecordEvent -> {
                    if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                        Log.d("CameraX", "Recording started");
                    } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                        Log.d("CameraX", "Recording finalized: " +
                                ((VideoRecordEvent.Finalize) videoRecordEvent).getOutputResults().getOutputUri());
                    }
                });
    }

    public void stopRecording() {
        if (currentRecording != null) {
            currentRecording.stop();
            currentRecording = null;
        }
    }

}
