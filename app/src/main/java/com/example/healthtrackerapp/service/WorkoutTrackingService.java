package com.example.healthtrackerapp.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.healthtrackerapp.R;
import com.example.healthtrackerapp.model.WorkoutLog;
import com.example.healthtrackerapp.view.activity.AddWorkoutLogActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;

public class WorkoutTrackingService extends Service implements SensorEventListener {
    private static final String TAG = "WorkoutTrackingService";
    private static final String CHANNEL_ID = "WorkoutTrackingChannel";
    private static final int NOTIFICATION_ID = 1;

    // Constants for different workout types
    private static class WorkoutParameters {
        final float threshold;    // Ngưỡng phát hiện bước
        final int minStepDelay;   // Thời gian tối thiểu giữa các bước (ms)

        WorkoutParameters(float threshold, int minStepDelay) {
            this.threshold = threshold;
            this.minStepDelay = minStepDelay;
        }
    }

    // Parameters for different workout types
    private static final Map<String, WorkoutParameters> WORKOUT_PARAMS = new HashMap<String, WorkoutParameters>() {{
        // Walking: Ngưỡng vừa phải, nhịp độ chậm
        put("Walking", new WorkoutParameters(10.0f, 300));

        // Running: Ngưỡng cao hơn, nhịp độ nhanh
        put("Running", new WorkoutParameters(15.0f, 200));

        // Cycling: Ngưỡng thấp, nhịp độ rất nhanh
        put("Cycling", new WorkoutParameters(8.0f, 150));
    }};

    // Current workout parameters
    private WorkoutParameters currentParams;
    private float threshold;
    private int minStepDelay;
    private long lastStepTime;
    private boolean isPeak = false;
    private float lastAcceleration = 0;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private String workoutType;
    private int totalSteps = 0;
    private long startTime;
    private boolean isTracking = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int durationMinutes = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        createNotificationChannel();
        Log.d(TAG, "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("workout_type")) {
            workoutType = intent.getStringExtra("workout_type");
            startTracking();
        } else {
            Log.e(TAG, "No workout_type provided in intent");
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private void startTracking() {
        if (accelerometer != null) {
            Log.d(TAG, "Starting workout tracking for type: " + workoutType);
            isTracking = true;
            startTime = System.currentTimeMillis();
            totalSteps = 0;
            lastStepTime = 0;
            durationMinutes = 0;

            // Update parameters based on workout type
            updateWorkoutParameters(workoutType);

            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            startForeground(NOTIFICATION_ID, createNotification());
            updateNotification();
            Log.d(TAG, "Service started in foreground with accelerometer");

            // Start duration tracking
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isTracking) {
                        durationMinutes++;
                        Log.d(TAG, "Duration updated to: " + durationMinutes + " minutes");
                        updateNotification();
                        handler.postDelayed(this, 60000);
                    }
                }
            }, 60000);
        } else {
            Log.e(TAG, "No accelerometer sensor found on device");
            stopSelf();
        }
    }

    private void stopTracking() {
        if (isTracking) {
            Log.d(TAG, "Stopping workout tracking");
            isTracking = false;
            sensorManager.unregisterListener(this);

            // Calculate calories based on workout type, duration, and steps
            int calories = calculateCalories(workoutType, durationMinutes, totalSteps);
            Log.d(TAG, "Final stats - Steps: " + totalSteps
                    + ", Calories: " + calories
                    + ", Duration: " + durationMinutes);

            // Stop the service - Note: Saving is now handled by AddWorkoutLogActivity
            stopForeground(true);
            stopSelf();
            Log.d(TAG, "Service stopped");
        }
    }

    private int calculateCalories(String workoutType, int durationMinutes, int steps) {
        double caloriesPerMinute;
        double caloriesPerStep;
        switch (workoutType) {
            case "Walking":
                caloriesPerMinute = 4.0;
                caloriesPerStep = 0.04;
                break;
            case "Running":
                caloriesPerMinute = 10.0;
                caloriesPerStep = 0.1;
                break;
            case "Cycling":
                caloriesPerMinute = 7.0;
                caloriesPerStep = 0.02;
                break;
            default:
                caloriesPerMinute = 5.0;
                caloriesPerStep = 0.05;
        }
        return (int) (caloriesPerMinute * durationMinutes + caloriesPerStep * steps);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Workout Tracking",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Shows workout tracking status");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
            Log.d(TAG, "Notification channel created");
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, AddWorkoutLogActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Đang theo dõi: " + workoutType)
                .setContentText("Thời gian: " + durationMinutes + " phút | Bước: " + totalSteps)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void updateNotification() {
        int currentCalories = calculateCalories(workoutType, durationMinutes, totalSteps);

        Log.d(TAG, "Broadcasting stats update - Steps: " + totalSteps
                + ", Calories: " + currentCalories
                + ", Duration: " + durationMinutes);

        // Update notification
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(NOTIFICATION_ID, createNotification());

        // Broadcast stats update
        Intent updateIntent = new Intent("com.example.healthtrackerapp.UPDATE_STATS");
        updateIntent.putExtra("steps", totalSteps);
        updateIntent.putExtra("calories", currentCalories);
        updateIntent.putExtra("duration", durationMinutes);

        LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent);
        Log.d(TAG, "Broadcast sent successfully");
    }

    private void updateWorkoutParameters(String type) {
        currentParams = WORKOUT_PARAMS.getOrDefault(type, WORKOUT_PARAMS.get("Walking"));
        threshold = currentParams.threshold;
        minStepDelay = currentParams.minStepDelay;
        lastStepTime = 0;
        isPeak = false;
        lastAcceleration = 0;

        Log.d(TAG, String.format("Updated parameters for %s - Threshold: %.1f, MinDelay: %dms",
            type, threshold, minStepDelay));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Tính tổng gia tốc (bỏ qua trọng lực)
            float acceleration = (float) Math.sqrt(
                event.values[0] * event.values[0] +
                event.values[1] * event.values[1] +
                event.values[2] * event.values[2]
            ) - 9.8f; // Trừ đi gia tốc trọng trường

            // Log dữ liệu khi có chuyển động đáng kể
            if (acceleration > threshold * 0.5f) {
                Log.d(TAG, String.format("%s - Accel: %.2f Steps: %d",
                    workoutType, acceleration, totalSteps));
            }

            // Phát hiện bước đơn giản dựa trên đỉnh gia tốc
            if (acceleration > threshold && !isPeak) {
                isPeak = true;
                long currentTime = System.currentTimeMillis();

                // Kiểm tra thời gian giữa các bước
                if (currentTime - lastStepTime > minStepDelay) {
                    totalSteps++;
                    lastStepTime = currentTime;
                    Log.d(TAG, String.format("%s step detected! Accel: %.2f Total: %d",
                        workoutType, acceleration, totalSteps));
                    updateNotification();
                }
            } else if (acceleration < threshold * 0.5f) {
                isPeak = false;
            }

            lastAcceleration = acceleration;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "Sensor accuracy changed: " + sensor.getType() + ", accuracy: " + accuracy);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTracking();
        Log.d(TAG, "Service destroyed");
    }
}