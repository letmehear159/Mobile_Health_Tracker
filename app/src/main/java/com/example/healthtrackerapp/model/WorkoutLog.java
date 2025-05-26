package com.example.healthtrackerapp.model;

public class WorkoutLog {
    private int id;
    private String date;
    private String workoutType;
    private int durationMinutes;
    private int calories;
    private Integer steps;
    private String notes;

    public WorkoutLog(int id, String date, String workoutType, int durationMinutes, int calories, Integer steps, String notes) {
        this.id = id;
        this.date = date;
        this.workoutType = workoutType;
        this.durationMinutes = durationMinutes;
        this.calories = calories;
        this.steps = steps;
        this.notes = notes;
    }

    public WorkoutLog(String workoutType, Integer steps, int calories, String date) {
        this.date = date;
        this.workoutType = workoutType;
        this.steps = steps;
        this.calories = calories;
    }

    // Getters and setters
    public int getId() { return id; }
    public String getDate() { return date; }
    public String getWorkoutType() { return workoutType; }
    public int getDurationMinutes() { return durationMinutes; }
    public int getCalories() { return calories; }
    public Integer getSteps() { return steps; }
    public String getNotes() { return notes; }
}
