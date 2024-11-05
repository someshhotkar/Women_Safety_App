package com.prabhu.womensafetyapp;

import java.util.List;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;

public class AccelerometerManager {

    private static Context aContext = null;

    /** Accuracy configuration */
    private static float threshold = 15.0f;
    private static int interval = 200;

    private static Sensor sensor;
    private static SensorManager sensorManager;
    private static AccelerometerListener listener;

    /** indicates whether or not Accelerometer Sensor is supported */
    private static Boolean supported;
    /** indicates whether or not Accelerometer Sensor is running */
    private static boolean running = false;

    private static final String CHANNEL_ID = "shake_detection_channel";

    /**
     * Returns true if the manager is listening to orientation changes
     */
    public static boolean isListening() {
        return running;
    }

    /**
     * Unregisters listeners
     */
    public static void stopListening() {
        running = false;
        try {
            if (sensorManager != null && sensorEventListener != null) {
                sensorManager.unregisterListener(sensorEventListener);
            }
        } catch (Exception e) {}
    }

    /**
     * Returns true if at least one Accelerometer sensor is available
     */
    public static boolean isSupported(Context context) {
        aContext = context;
        if (supported == null) {
            if (aContext != null) {
                sensorManager = (SensorManager) aContext.getSystemService(Context.SENSOR_SERVICE);
                // Get all sensors in device
                List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
                supported = new Boolean(sensors.size() > 0);
            } else {
                supported = Boolean.FALSE;
            }
        }
        return supported;
    }

    /**
     * Configure the listener for shaking
     * @param threshold minimum acceleration variation for considering shaking
     * @param interval minimum interval between two shake events
     */
    public static void configure(int threshold, int interval) {
        AccelerometerManager.threshold = threshold;
        AccelerometerManager.interval = interval;
    }

    /**
     * Registers a listener and start listening
     * @param accelerometerListener callback for accelerometer events
     */
    public static void startListening(AccelerometerListener accelerometerListener) {
        sensorManager = (SensorManager) aContext.getSystemService(Context.SENSOR_SERVICE);
        // Take all sensors in device
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (sensors.size() > 0) {
            sensor = sensors.get(0);
            // Register Accelerometer Listener
            running = sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_GAME);
            listener = accelerometerListener;
        }
    }

    /**
     * Configures threshold and interval
     * And registers a listener and start listening
     * @param accelerometerListener callback for accelerometer events
     * @param threshold minimum acceleration variation for considering shaking
     * @param interval minimum interval between two shake events
     */
    public static void startListening(AccelerometerListener accelerometerListener, int threshold, int interval) {
        configure(threshold, interval);
        startListening(accelerometerListener);
    }

    /**
     * The listener that listens to events from the accelerometer
     */
    private static SensorEventListener sensorEventListener = new SensorEventListener() {

        private long now = 0;
        private long timeDiff = 0;
        private long lastUpdate = 0;
        private long lastShake = 0;

        private float x = 0;
        private float y = 0;
        private float z = 0;
        private float lastX = 0;
        private float lastY = 0;
        private float lastZ = 0;
        private float force = 0;

        public void onAccuracyChanged(Sensor sensor, int accuracy) {}

        public void onSensorChanged(SensorEvent event) {
            now = event.timestamp;
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];

            if (lastUpdate == 0) {
                lastUpdate = now;
                lastShake = now;
                lastX = x;
                lastY = y;
                lastZ = z;
                showNotification("No Motion detected");
            } else {
                timeDiff = now - lastUpdate;
                if (timeDiff > 0) {
                    force = Math.abs(x + y + z - lastX - lastY - lastZ);

                    if (Float.compare(force, threshold) > 0) {
                        if (now - lastShake >= interval) {
                            // trigger shake event
                            listener.onShake(force);
                        } else {
                            showNotification("No Motion detected.");
                        }
                        lastShake = now;
                    }
                    lastX = x;
                    lastY = y;
                    lastZ = z;
                    lastUpdate = now;
                } else {
                    showNotification("No Motion detected");
                }
            }
            // trigger change event
            listener.onAccelerationChanged(x, y, z);
        }
    };

    // Method to show toast message
    private static void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Context appContext = aContext.getApplicationContext();
            Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show();
        });
    }

    // Method to show notification instead of toast
    private static void showNotification(String message) {
        Context appContext = aContext.getApplicationContext();
        NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Shake Detection", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(appContext, CHANNEL_ID)
                .setContentTitle("Shake Detected")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher) // Replace with your icon
                .build();

        notificationManager.notify(1, notification);
    }
}
