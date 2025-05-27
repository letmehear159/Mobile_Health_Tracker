package com.example.healthtrackerapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.healthtrackerapp.model.Appointment;
import com.example.healthtrackerapp.repository.AppointmentRepository;

import java.util.List;

public class AppointmentViewModel extends AndroidViewModel {

    private AppointmentRepository repository;

    public AppointmentViewModel(@NonNull Application application) {
        super(application);
        repository = new AppointmentRepository(application);
    }

    public LiveData<List<Appointment>> getAppointmentsForUser(String userId) {
        return repository.getAppointmentsForUser(userId);
    }

    public void insert(Appointment appointment) {
        repository.insert(appointment);
    }

    public void delete(Appointment appointment) {
        repository.delete(appointment);
    }

    public void update(Appointment appointment) {
        repository.update(appointment);
    }
}

