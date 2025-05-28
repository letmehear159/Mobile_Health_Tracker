package com.example.healthtrackerapp.view.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.healthtrackerapp.R;
import com.example.healthtrackerapp.model.WorkoutLog;
import com.example.healthtrackerapp.service.WorkoutTrackingService;
import com.example.healthtrackerapp.viewmodel.WorkoutLogViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddWorkoutLogActivity extends AppCompatActivity {
    private static final String TAG = "AddWorkoutLogActivity";
    private static final String ACTION_UPDATE_STATS = "com.example.healthtrackerapp.UPDATE_STATS";
    private static final String EXTRA_STEPS = "steps";
    private static final String EXTRA_CALORIES = "calories";
    private static final String EXTRA_DURATION = "duration";

    private WorkoutLogViewModel viewModel;
    private Spinner workoutTypeSpinner;
    private MaterialButton startTrackingButton;
    private FloatingActionButton addManualButton;
    private TextView statusText;
    private TextView stepsText;
    private TextView caloriesText;
    private boolean isTracking = false;
    private SensorManager sensorManager;
    private BroadcastReceiver statsReceiver;

    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
            boolean allGranted = true;
            for (Boolean isGranted : permissions.values()) {
                if (!isGranted) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                Log.d(TAG, "All permissions granted");
                startTracking();
            } else {
                Log.d(TAG, "Some permissions were denied");
                Toast.makeText(this, "Cần cấp đầy đủ quyền để theo dõi hoạt động", Toast.LENGTH_LONG).show();
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_workout_log);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(WorkoutLogViewModel.class);

        // Initialize sensor manager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Initialize views
        workoutTypeSpinner = findViewById(R.id.workoutTypeSpinner);
        startTrackingButton = findViewById(R.id.startTrackingButton);
        addManualButton = findViewById(R.id.addManualButton);
        statusText = findViewById(R.id.statusText);
        stepsText = findViewById(R.id.stepsText);
        caloriesText = findViewById(R.id.caloriesText);

        // Setup spinner
        String[] workoutTypes = {"Walking", "Running", "Cycling"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            workoutTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        workoutTypeSpinner.setAdapter(adapter);

        // Setup start tracking button
        startTrackingButton.setOnClickListener(v -> {
            if (!isTracking) {
                if (checkStepSensor()) {
                    startTracking();
                } else {
                    showNoStepSensorDialog();
                }
            } else {
                stopTracking();
            }
        });

        addManualButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddManualWorkoutLogActivity.class);
            startActivity(intent);
        });

        // Setup broadcast receiver for stats updates
        setupStatsReceiver();
    }

    private void setupStatsReceiver() {
        Log.d(TAG, "Setting up stats receiver");
        statsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Received broadcast with action: " + intent.getAction());
                if (ACTION_UPDATE_STATS.equals(intent.getAction())) {
                    int steps = intent.getIntExtra(EXTRA_STEPS, 0);
                    int calories = intent.getIntExtra(EXTRA_CALORIES, 0);
                    int duration = intent.getIntExtra(EXTRA_DURATION, 0);
                    
                    Log.d(TAG, "Received stats update - Steps: " + steps 
                        + ", Calories: " + calories 
                        + ", Duration: " + duration);
                    
                    updateStatsDisplay(steps, calories, duration);
                }
            }
        };

        IntentFilter filter = new IntentFilter(ACTION_UPDATE_STATS);
        LocalBroadcastManager.getInstance(this).registerReceiver(statsReceiver, filter);
        Log.d(TAG, "Stats receiver registered with filter: " + ACTION_UPDATE_STATS);
    }

    private void updateStatsDisplay(int steps, int calories, int duration) {
        Log.d(TAG, "Updating UI with stats - Steps: " + steps 
            + ", Calories: " + calories 
            + ", Duration: " + duration);
            
        stepsText.setText(String.valueOf(steps));
        caloriesText.setText(String.valueOf(calories));
        statusText.setText(String.format("Đang theo dõi: %s (%d phút)", 
            workoutTypeSpinner.getSelectedItem().toString(), duration));
    }

    private boolean checkStepSensor() {
        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        return stepSensor != null;
    }

    private void showNoStepSensorDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Không tìm thấy cảm biến bước chân")
            .setMessage("Thiết bị của bạn không có cảm biến bước chân hoặc cảm biến không hoạt động. Tính năng theo dõi workout có thể không hoạt động chính xác.")
            .setPositiveButton("Vẫn tiếp tục", (dialog, which) -> startTracking())
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void startTracking() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            String[] permissions;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                permissions = new String[]{
                    Manifest.permission.ACTIVITY_RECOGNITION,
                    Manifest.permission.FOREGROUND_SERVICE_HEALTH
                };
            } else {
                permissions = new String[]{
                    Manifest.permission.ACTIVITY_RECOGNITION
                };
            }

            boolean allGranted = true;
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (!allGranted) {
                Log.d(TAG, "Requesting permissions: " + String.join(", ", permissions));
                requestPermissionsLauncher.launch(permissions);
                return;
            }
        }

        String selectedWorkoutType = workoutTypeSpinner.getSelectedItem().toString();
        Log.d(TAG, "Starting tracking for workout type: " + selectedWorkoutType);
        
        Intent serviceIntent = new Intent(this, WorkoutTrackingService.class);
        serviceIntent.putExtra("workout_type", selectedWorkoutType);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "Starting foreground service");
            startForegroundService(serviceIntent);
        } else {
            Log.d(TAG, "Starting regular service");
            startService(serviceIntent);
        }

        isTracking = true;
        startTrackingButton.setText("Dừng theo dõi");
        workoutTypeSpinner.setEnabled(false);
        
        // Reset stats display
        updateStatsDisplay(0, 0, 0);
        Log.d(TAG, "Tracking started, UI updated");
    }

    private void stopTracking() {
        Log.d(TAG, "Stopping tracking");
        Intent serviceIntent = new Intent(this, WorkoutTrackingService.class);
        stopService(serviceIntent);

        // Create new WorkoutLog from tracking data
        String selectedWorkoutType = workoutTypeSpinner.getSelectedItem().toString();
        int steps = Integer.parseInt(stepsText.getText().toString());
        int calories = Integer.parseInt(caloriesText.getText().toString());
        int duration = Integer.parseInt(statusText.getText().toString().replaceAll("[^0-9]", ""));
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        WorkoutLog newLog = new WorkoutLog(
            0, // ID will be auto-generated
            currentDate,
            selectedWorkoutType,
            duration,
            calories,
            steps,
            "Tự động theo dõi (Accelerometer)"
        );

        // Use ViewModel to save the log
        viewModel.insertWorkoutLog(newLog);

        isTracking = false;
        startTrackingButton.setText("Bắt đầu theo dõi");
        statusText.setText("Đã dừng theo dõi");
        workoutTypeSpinner.setEnabled(true);
        Log.d(TAG, "Tracking stopped, UI updated");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Activity being destroyed");
        if (isTracking) {
            stopTracking();
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(statsReceiver);
        Log.d(TAG, "Stats receiver unregistered");
    }
}