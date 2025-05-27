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
import com.example.healthtrackerapp.database.DatabaseHelper;
import com.example.healthtrackerapp.model.WorkoutLog;
import com.example.healthtrackerapp.view.activity.AddWorkoutLogActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

public class WorkoutTrackingService extends Service implements SensorEventListener {
    private static final String TAG = "WorkoutTrackingService";
    private static final String CHANNEL_ID = "WorkoutTrackingChannel";
    private static final int NOTIFICATION_ID = 1;

    // Constants for step detection
    private static final float STEP_THRESHOLD = 8.0f; // Tuned for walking
    private static final float RESET_THRESHOLD = STEP_THRESHOLD * 0.2f; // Reset at 20% of threshold
    private static final int STEP_DELAY_NS = 250000000; // 250 ms for walking cadence
    private static final int WINDOW_SIZE = 5; // Smooth over 5 samples
    private static final float ALPHA = 0.8f; // Stronger low-pass filter

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private DatabaseHelper dbHelper;
    private String workoutType;
    private int totalSteps = 0;
    private long lastStepTimeNs = 0;
    private float lastMagnitude = 0;
    private boolean isPeak = false;
    private Queue<Float> magnitudeWindow = new LinkedList<>();
    private float[] gravity = new float[3];
    private float[] linearAcceleration = new float[3];
    private long startTime;
    private boolean isTracking = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int durationMinutes = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        dbHelper = new DatabaseHelper(this);
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
            lastStepTimeNs = 0;
            lastMagnitude = 0;
            durationMinutes = 0;
            magnitudeWindow.clear(); // Clear window on start

            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            startForeground(NOTIFICATION_ID, createNotification());
            updateNotification(); // Send initial stats
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

            // Save workout log
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String currentDate = dateFormat.format(new Date());

            WorkoutLog log = new WorkoutLog(
                    0, // ID auto-generated
                    currentDate,
                    workoutType,
                    durationMinutes,
                    calories,
                    totalSteps,
                    "Tự động theo dõi (Accelerometer)"
            );

            dbHelper.insertWorkoutLog(log);
            Log.d(TAG, "Workout log saved to database");

            // Stop the service
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Apply low-pass filter
            gravity[0] = ALPHA * gravity[0] + (1 - ALPHA) * event.values[0];
            gravity[1] = ALPHA * gravity[1] + (1 - ALPHA) * event.values[1];
            gravity[2] = ALPHA * gravity[2] + (1 - ALPHA) * event.values[2];

            // Remove gravity contribution
            linearAcceleration[0] = event.values[0] - gravity[0];
            linearAcceleration[1] = event.values[1] - gravity[1];
            linearAcceleration[2] = event.values[2] - gravity[2];

            // Calculate magnitude
            float magnitude = (float) Math.sqrt(
                    linearAcceleration[0] * linearAcceleration[0] +
                            linearAcceleration[1] * linearAcceleration[1] +
                            linearAcceleration[2] * linearAcceleration[2]
            );

            // Focus on z-axis for walking
            float zAcceleration = Math.abs(linearAcceleration[2]);

            // Add to sliding window
            magnitudeWindow.add(magnitude);
            if (magnitudeWindow.size() > WINDOW_SIZE) {
                magnitudeWindow.poll();
            }

            // Calculate average magnitude
            float avgMagnitude = 0;
            for (float m : magnitudeWindow) {
                avgMagnitude += m;
            }
            avgMagnitude /= magnitudeWindow.size();

            // Log sensor data for debugging
            if (zAcceleration > STEP_THRESHOLD * 0.5f || avgMagnitude > STEP_THRESHOLD * 0.5f) {
                Log.d(TAG, String.format("Raw - X: %.2f, Y: %.2f, Z: %.2f | Linear - X: %.2f, Y: %.2f, Z: %.2f | Magnitude: %.2f, Z-Accel: %.2f, Steps: %d",
                        event.values[0], event.values[1], event.values[2],
                        linearAcceleration[0], linearAcceleration[1], linearAcceleration[2],
                        avgMagnitude, zAcceleration, totalSteps));
            }

            // Detect step (combine magnitude and z-axis)
            if (avgMagnitude > STEP_THRESHOLD && zAcceleration > STEP_THRESHOLD * 0.7f && !isPeak) {
                isPeak = true;
                long currentTimeNs = event.timestamp;

                if (currentTimeNs - lastStepTimeNs > STEP_DELAY_NS) {
                    totalSteps++;
                    lastStepTimeNs = currentTimeNs;
                    Log.d(TAG, String.format("Step detected! Z-Accel: %.2f, Magnitude: %.2f, Total steps: %d",
                            zAcceleration, avgMagnitude, totalSteps));
                    updateNotification();
                }
            } else if (avgMagnitude < RESET_THRESHOLD && zAcceleration < RESET_THRESHOLD) {
                isPeak = false;
            }

            lastMagnitude = avgMagnitude;
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