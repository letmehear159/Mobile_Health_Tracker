package com.example.healthtrackerapp.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "appointments")
public class Appointment {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String doctorName;
    public String date;
    public String time;
    public String location;
    public String ticketCode;

    // Constructor
    public Appointment(String doctorName, String date, String time, String location, String ticketCode) {
        this.doctorName = doctorName;
        this.date = date;
        this.time = time;
        this.location = location;
        this.ticketCode = ticketCode;
    }

    // Getter & Setter (nếu cần thêm IDE có thể generate)
}
