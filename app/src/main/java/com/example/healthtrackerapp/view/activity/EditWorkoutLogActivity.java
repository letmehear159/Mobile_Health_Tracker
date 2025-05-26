package com.example.healthtrackerapp.view.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.healthtrackerapp.R;
import com.example.healthtrackerapp.model.WorkoutLog;
import com.example.healthtrackerapp.viewmodel.WorkoutLogViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditWorkoutLogActivity extends AppCompatActivity {
    private WorkoutLogViewModel viewModel;
    private Calendar selectedDate;
    private SimpleDateFormat dateFormat;
    private int workoutLogId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_workout_log);

        // Get workout log data from intent
        workoutLogId = getIntent().getIntExtra("workout_log_id", -1);
        android.util.Log.d("EditWorkoutLogActivity", "Received workout_log_id: " + workoutLogId);
        
        if (workoutLogId == -1) {
            android.util.Log.e("EditWorkoutLogActivity", "Invalid workout log ID received");
            Toast.makeText(this, "Không tìm thấy workout log", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(WorkoutLogViewModel.class);
        selectedDate = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Initialize UI components
        TextInputEditText dateInput = findViewById(R.id.dateInput);
        TextInputEditText workoutTypeInput = findViewById(R.id.workoutTypeInput);
        TextInputEditText durationInput = findViewById(R.id.durationInput);
        TextInputEditText caloriesInput = findViewById(R.id.caloriesInput);
        TextInputEditText stepsInput = findViewById(R.id.stepsInput);
        TextInputEditText notesInput = findViewById(R.id.notesInput);
        MaterialButton saveButton = findViewById(R.id.saveButton);
        MaterialButton deleteButton = findViewById(R.id.deleteButton);
        View editWorkoutTitle = findViewById(R.id.editWorkoutTitle);
        View loadingProgressBar = findViewById(R.id.loadingProgressBar);
        View cardView = findViewById(R.id.cardView);

        // Show loading state and hide content
        editWorkoutTitle.setVisibility(View.GONE);
        cardView.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.VISIBLE);

        // Load workout log data
        android.util.Log.d("EditWorkoutLogActivity", "Requesting workout log data for ID: " + workoutLogId);
        
        // Get the LiveData first
        LiveData<WorkoutLog> workoutLogLiveData = viewModel.getWorkoutLogById(workoutLogId);
        
        // Then observe it
        workoutLogLiveData.observe(this, workoutLog -> {
            android.util.Log.d("EditWorkoutLogActivity", "Received workout log data: " + (workoutLog != null ? "not null" : "null"));
            
            if (workoutLog != null) {
                try {
                    // Hide loading state and show content
                    loadingProgressBar.setVisibility(View.GONE);
                    editWorkoutTitle.setVisibility(View.VISIBLE);
                    cardView.setVisibility(View.VISIBLE);

                    // Parse and set the date
                    selectedDate.setTime(dateFormat.parse(workoutLog.getDate()));
                    dateInput.setText(workoutLog.getDate());
                    android.util.Log.d("EditWorkoutLogActivity", "Set date: " + workoutLog.getDate());
                    
                    // Set other fields
                    workoutTypeInput.setText(workoutLog.getWorkoutType());
                    durationInput.setText(String.valueOf(workoutLog.getDurationMinutes()));
                    caloriesInput.setText(String.valueOf(workoutLog.getCalories()));
                    android.util.Log.d("EditWorkoutLogActivity", "Set basic fields - Type: " + workoutLog.getWorkoutType() 
                        + ", Duration: " + workoutLog.getDurationMinutes() 
                        + ", Calories: " + workoutLog.getCalories());
                    
                    // Set optional fields if they exist
                    if (workoutLog.getSteps() != null) {
                        stepsInput.setText(String.valueOf(workoutLog.getSteps()));
                        android.util.Log.d("EditWorkoutLogActivity", "Set steps: " + workoutLog.getSteps());
                    } else {
                        stepsInput.setText("");
                    }
                    
                    if (workoutLog.getNotes() != null && !workoutLog.getNotes().isEmpty()) {
                        notesInput.setText(workoutLog.getNotes());
                        android.util.Log.d("EditWorkoutLogActivity", "Set notes: " + workoutLog.getNotes());
                    } else {
                        notesInput.setText("");
                    }
                } catch (Exception e) {
                    android.util.Log.e("EditWorkoutLogActivity", "Error setting workout log data", e);
                    Toast.makeText(this, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                // Only show error if we've waited a reasonable time for the data
                if (workoutLogLiveData.getValue() == null) {
                    // Add a small delay to ensure we're not showing the error too early
                    new Handler().postDelayed(() -> {
                        if (workoutLogLiveData.getValue() == null) {
                            android.util.Log.e("EditWorkoutLogActivity", "No workout log data found for ID: " + workoutLogId);
                            Toast.makeText(this, "Không tìm thấy dữ liệu workout log", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }, 1000); // Wait 1 second before showing error
                }
            }
        });

        // Setup date picker
        dateInput.setOnClickListener(v -> showDatePicker());

        // Setup save button
        saveButton.setOnClickListener(v -> {
            String date = dateInput.getText() != null ? dateInput.getText().toString() : "";
            String workoutType = workoutTypeInput.getText() != null ? workoutTypeInput.getText().toString() : "";
            String durationStr = durationInput.getText() != null ? durationInput.getText().toString() : "";
            String caloriesStr = caloriesInput.getText() != null ? caloriesInput.getText().toString() : "";
            String stepsStr = stepsInput.getText() != null ? stepsInput.getText().toString() : "";
            String notes = notesInput.getText() != null ? notesInput.getText().toString() : "";

            // Validate inputs
            if (date.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ngày", Toast.LENGTH_SHORT).show();
                return;
            }
            if (workoutType.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập loại bài tập", Toast.LENGTH_SHORT).show();
                return;
            }
            if (durationStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập thời gian", Toast.LENGTH_SHORT).show();
                return;
            }
            if (caloriesStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập calo", Toast.LENGTH_SHORT).show();
                return;
            }

            int durationMinutes;
            int calories;
            Integer steps = null;

            try {
                durationMinutes = Integer.parseInt(durationStr);
                calories = Integer.parseInt(caloriesStr);
                if (!stepsStr.isEmpty()) {
                    steps = Integer.parseInt(stepsStr);
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Vui lòng nhập số hợp lệ cho thời gian, calo hoặc số bước", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update workout log
            WorkoutLog updatedLog = new WorkoutLog(workoutLogId, date, workoutType, durationMinutes, calories, steps, notes);
            viewModel.updateWorkoutLog(updatedLog);
            Toast.makeText(this, "Đã cập nhật workout log", Toast.LENGTH_SHORT).show();
            finish();
        });

        // Setup delete button
        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa workout log này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.deleteWorkoutLog(workoutLogId);
                    Toast.makeText(this, "Đã xóa workout log", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
        });
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedDate.set(year, month, dayOfMonth);
                TextInputEditText dateInput = findViewById(R.id.dateInput);
                dateInput.setText(dateFormat.format(selectedDate.getTime()));
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        // Set max date to today
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }
} 