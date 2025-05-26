package com.example.healthtrackerapp.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.healthtrackerapp.model.Appointment;

import java.util.List;

public class AppointmentRepository {

    private AppointmentDao appointmentDao;

    public AppointmentRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        appointmentDao = db.appointmentDao();
    }

    public LiveData<List<Appointment>> getAppointmentsForUser(String userId) {
        return appointmentDao.getAppointmentsForUser(userId);
    }

    public void insert(Appointment appointment) {
        AppDatabase.databaseWriteExecutor.execute(() -> appointmentDao.insert(appointment));
    }

    public void delete(Appointment appointment) {
        AppDatabase.databaseWriteExecutor.execute(() -> appointmentDao.delete(appointment));
    }

    public void update(Appointment appointment) {
        AppDatabase.databaseWriteExecutor.execute(() -> appointmentDao.update(appointment));
    }
}

