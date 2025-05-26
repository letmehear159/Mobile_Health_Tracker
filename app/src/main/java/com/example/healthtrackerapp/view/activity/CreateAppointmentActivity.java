package com.example.healthtrackerapp.view.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.healthtrackerapp.R;
import com.example.healthtrackerapp.model.Appointment;
import com.example.healthtrackerapp.viewmodel.AppointmentViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.Locale;

public class CreateAppointmentActivity extends AppCompatActivity {

    private EditText etDoctor, etDate, etTime, etLocation, etTicket;
    private AppointmentViewModel viewModel;
    private int editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_appointment);

        etDoctor = findViewById(R.id.etDoctorName);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etLocation = findViewById(R.id.etLocation);
        etTicket = findViewById(R.id.etTicket);

        Button btnCreate = findViewById(R.id.btnCreate);
        Button btnCancel = findViewById(R.id.btnCancel);

        viewModel = new ViewModelProvider(this).get(AppointmentViewModel.class);

        // 🔹 Gắn DatePicker
        etDate.setFocusable(false);
        etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                etDate.setText(date);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        // 🔹 Gắn TimePicker
        etTime.setFocusable(false);
        etTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                etTime.setText(time);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        });

        // 🔹 Nhận dữ liệu nếu đang sửa
        Intent intent = getIntent();
        if (intent != null) {
            editId = intent.getIntExtra("edit_id", -1);
            if (editId != -1) {
                etDoctor.setText(intent.getStringExtra("doctor"));
                etDate.setText(intent.getStringExtra("date"));
                etTime.setText(intent.getStringExtra("time"));
                etLocation.setText(intent.getStringExtra("location"));
                etTicket.setText(intent.getStringExtra("ticket"));
            }
        }

        // 🔹 Nút tạo hoặc cập nhật
        btnCreate.setOnClickListener(v -> {
            String doctor = etDoctor.getText().toString();
            String date = etDate.getText().toString();
            String time = etTime.getText().toString();
            String location = etLocation.getText().toString();
            String ticket = etTicket.getText().toString();

            if (!doctor.isEmpty() && !date.isEmpty() && !time.isEmpty()) {
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Appointment a = new Appointment(doctor, date, time, location, ticket);
                a.userId = currentUserId;

                if (editId != -1) {
                    a.id = editId;
                    viewModel.update(a);
                } else {
                    viewModel.insert(a);
                }
                finish();
            }
        });

        btnCancel.setOnClickListener(v -> finish());
    }
}
