package com.example.healthtrackerapp.repository;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.healthtrackerapp.model.Appointment;

import java.util.List;

@Dao
public interface AppointmentDao {

    @Insert
    void insert(Appointment appointment);

    @Query("SELECT * FROM appointments WHERE userId = :userId ORDER BY date ASC")
    LiveData<List<Appointment>> getAppointmentsForUser(String userId);

    @Delete
    void delete(Appointment appointment);

    @Update
    void update(Appointment appointment);

}
