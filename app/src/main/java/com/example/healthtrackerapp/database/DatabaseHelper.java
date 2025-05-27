package com.example.healthtrackerapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.healthtrackerapp.model.WorkoutLog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "health_database.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "workout_logs";
    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "date TEXT NOT NULL," +
                    "workout_type TEXT NOT NULL," +
                    "duration_minutes INTEGER NOT NULL," +
                    "calories INTEGER NOT NULL," +
                    "steps INTEGER," +
                    "notes TEXT)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertWorkoutLog(WorkoutLog log) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("date", log.getDate());
            values.put("workout_type", log.getWorkoutType());
            values.put("duration_minutes", log.getDurationMinutes());
            values.put("calories", log.getCalories());
            values.put("steps", log.getSteps());
            values.put("notes", log.getNotes());
            db.insert(TABLE_NAME, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<WorkoutLog> getLogsForWeek(String weekStart) {
        List<WorkoutLog> logs = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, "date LIKE ?", new String[]{weekStart + "%"}, null, null, "date DESC");
        while (cursor.moveToNext()) {
            logs.add(cursorToWorkoutLog(cursor));
        }
        cursor.close();
        db.close();
        return logs;
    }

    public List<WorkoutLog> getAllLogs() {
        List<WorkoutLog> logs = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        
        try {
            db = getReadableDatabase();
            cursor = db.query(TABLE_NAME, null, null, null, null, null, "date DESC");
            while (cursor.moveToNext()) {
                logs.add(cursorToWorkoutLog(cursor));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return logs;
    }

    public List<WorkoutLog> getLogsByFilter(String filter) {
        List<WorkoutLog> logs = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        
        try {
            db = getReadableDatabase();
            String selection = null;
            String[] selectionArgs = null;

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String currentDate = dateFormat.format(calendar.getTime());

            switch (filter) {
                case "Hôm nay":
                    selection = "date = ?";
                    selectionArgs = new String[]{currentDate};
                    break;
                case "Tuần này":
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                    String weekStart = dateFormat.format(calendar.getTime());
                    selection = "date >= ? AND date <= ?";
                    selectionArgs = new String[]{weekStart, currentDate};
                    break;
                case "Tháng này":
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    String monthStart = dateFormat.format(calendar.getTime());
                    selection = "date >= ? AND date <= ?";
                    selectionArgs = new String[]{monthStart, currentDate};
                    break;
                case "Năm nay":
                    calendar.set(Calendar.DAY_OF_YEAR, 1);
                    String yearStart = dateFormat.format(calendar.getTime());
                    selection = "date >= ? AND date <= ?";
                    selectionArgs = new String[]{yearStart, currentDate};
                    break;
                default:
                    // Mặc định lấy tất cả
                    break;
            }

            cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, "date ASC");
            while (cursor.moveToNext()) {
                logs.add(cursorToWorkoutLog(cursor));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return logs;
    }

    private WorkoutLog cursorToWorkoutLog(Cursor cursor) {
        try {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
            String workoutType = cursor.getString(cursor.getColumnIndexOrThrow("workout_type"));
            int durationMinutes = cursor.getInt(cursor.getColumnIndexOrThrow("duration_minutes"));
            int calories = cursor.getInt(cursor.getColumnIndexOrThrow("calories"));
            Integer steps = cursor.isNull(cursor.getColumnIndexOrThrow("steps")) ? null : cursor.getInt(cursor.getColumnIndexOrThrow("steps"));
            String notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
            
            android.util.Log.d("DatabaseHelper", "Converting cursor to WorkoutLog - ID: " + id 
                + ", Type: " + workoutType 
                + ", Date: " + date);
            
            return new WorkoutLog(id, date, workoutType, durationMinutes, calories, steps, notes);
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "Error converting cursor to WorkoutLog", e);
            throw e;
        }
    }

    public void clearAllData() {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            db.delete(TABLE_NAME, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public WorkoutLog getWorkoutLogById(int id) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        WorkoutLog log = null;
        
        try {
            android.util.Log.d("DatabaseHelper", "Querying database for workout log ID: " + id);
            db = getReadableDatabase();
            cursor = db.query(TABLE_NAME, null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);
            
            android.util.Log.d("DatabaseHelper", "Cursor count: " + (cursor != null ? cursor.getCount() : 0));
            
            if (cursor != null && cursor.moveToFirst()) {
                log = cursorToWorkoutLog(cursor);
                android.util.Log.d("DatabaseHelper", "Found workout log - ID: " + log.getId() 
                    + ", Type: " + log.getWorkoutType() 
                    + ", Date: " + log.getDate());
            } else {
                android.util.Log.e("DatabaseHelper", "No workout log found for ID: " + id);
            }
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "Error getting workout log by ID: " + id, e);
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return log;
    }

    public void updateWorkoutLog(WorkoutLog log) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("date", log.getDate());
            values.put("workout_type", log.getWorkoutType());
            values.put("duration_minutes", log.getDurationMinutes());
            values.put("calories", log.getCalories());
            values.put("steps", log.getSteps());
            values.put("notes", log.getNotes());
            
            db.update(TABLE_NAME, values, "id = ?", new String[]{String.valueOf(log.getId())});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteWorkoutLog(int id) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            db.delete(TABLE_NAME, "id = ?", new String[]{String.valueOf(id)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void listAllWorkoutLogs() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            cursor = db.query(TABLE_NAME, null, null, null, null, null, "id ASC");
            
            android.util.Log.d("DatabaseHelper", "Total workout logs in database: " + cursor.getCount());
            
            while (cursor.moveToNext()) {
                WorkoutLog log = cursorToWorkoutLog(cursor);
                android.util.Log.d("DatabaseHelper", "Workout Log - ID: " + log.getId() 
                    + ", Type: " + log.getWorkoutType() 
                    + ", Date: " + log.getDate());
            }
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "Error listing workout logs", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }
}
