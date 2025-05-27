package com.example.healthtrackerapp.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "appointments")
public class Appointment {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String userId;
    public String doctorName;
    public String date;
    public String time;
    public String location;
    public String ticketCode;

    // Constructor đầy đủ
    public Appointment(String userId, String doctorName, String date, String time, String location, String ticketCode) {
        this.userId = userId;
        this.doctorName = doctorName;
        this.date = date;
        this.time = time;
        this.location = location;
        this.ticketCode = ticketCode;
    }

    // Constructor phụ trợ nếu cần (không có userId)
    @Ignore
    public Appointment(String doctorName, String date, String time, String location, String ticketCode) {
        this.doctorName = doctorName;
        this.date = date;
        this.time = time;
        this.location = location;
        this.ticketCode = ticketCode;
    }
}

