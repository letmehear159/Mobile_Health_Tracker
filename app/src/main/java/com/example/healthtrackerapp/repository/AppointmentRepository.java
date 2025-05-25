package com.example.healthtrackerapp.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.healthtrackerapp.model.Appointment;

import java.util.List;

public class AppointmentRepository {

    private AppointmentDao appointmentDao;
    private LiveData<List<Appointment>> allAppointments;

    public AppointmentRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        appointmentDao = db.appointmentDao();
        allAppointments = appointmentDao.getAllAppointments();
    }

    public LiveData<List<Appointment>> getAllAppointments() {
        return allAppointments;
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
