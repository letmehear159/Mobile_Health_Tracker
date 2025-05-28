package com.example.healthtrackerapp.view.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.healthtrackerapp.R;
import com.example.healthtrackerapp.model.WorkoutLog;
import com.example.healthtrackerapp.viewmodel.WorkoutLogViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddManualWorkoutLogActivity extends AppCompatActivity {
    private WorkoutLogViewModel viewModel;
    private Calendar selectedDate;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_manual_workout_log);

        viewModel = new ViewModelProvider(this).get(WorkoutLogViewModel.class);
        selectedDate = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        TextInputEditText dateInput = findViewById(R.id.dateInput);
        TextInputEditText workoutTypeInput = findViewById(R.id.workoutTypeInput);
        TextInputEditText durationInput = findViewById(R.id.durationInput);
        TextInputEditText caloriesInput = findViewById(R.id.caloriesInput);
        TextInputEditText stepsInput = findViewById(R.id.stepsInput);
        TextInputEditText notesInput = findViewById(R.id.notesInput);
        MaterialButton saveButton = findViewById(R.id.saveButton);

        // Set current date as default
        dateInput.setText(dateFormat.format(selectedDate.getTime()));

        // Setup date picker
        dateInput.setOnClickListener(v -> showDatePicker());

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

            // Save to database via ViewModel
            WorkoutLog workoutLog = new WorkoutLog(0, date, workoutType, durationMinutes, calories, steps, notes);
            viewModel.insertWorkoutLog(workoutLog);
            Toast.makeText(this, "Đã lưu workout log", Toast.LENGTH_SHORT).show();
            finish();
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