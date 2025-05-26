package com.example.healthtrackerapp.viewmodel;
import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.healthtrackerapp.database.DatabaseHelper;
import com.example.healthtrackerapp.model.WorkoutLog;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WorkoutLogViewModel extends AndroidViewModel {
    private final DatabaseHelper dbHelper;
    private final MutableLiveData<List<WorkoutLog>> workoutLogs = new MutableLiveData<>();
    private final MutableLiveData<String> summary = new MutableLiveData<>();
    private final MutableLiveData<List<Entry>> stepsData = new MutableLiveData<>();
    private final MutableLiveData<List<BarEntry>> caloriesData = new MutableLiveData<>();
    private final MutableLiveData<List<PieEntry>> workoutTypeData = new MutableLiveData<>();
    private final MutableLiveData<WorkoutLog> currentWorkoutLog = new MutableLiveData<>();
    private String currentFilter = "Tuần này";
    private final MutableLiveData<List<String>> chartLabels = new MutableLiveData<>();

    public WorkoutLogViewModel(Application application) {
        super(application);
        dbHelper = new DatabaseHelper(application);
        // Check if we need to insert dummy data
        checkAndInsertDummyData();
        loadData();
    }

    public LiveData<List<WorkoutLog>> getWorkoutLogs() {
        return workoutLogs;
    }

    public LiveData<String> getSummary() {
        return summary;
    }

    public LiveData<List<Entry>> getStepsData() {
        return stepsData;
    }

    public LiveData<List<BarEntry>> getCaloriesData() {
        return caloriesData;
    }

    public LiveData<List<PieEntry>> getWorkoutTypeData() {
        return workoutTypeData;
    }

    public LiveData<List<String>> getChartLabels() {
        return chartLabels;
    }

    public LiveData<WorkoutLog> getWorkoutLogById(int id) {
        android.util.Log.d("WorkoutLogViewModel", "Getting workout log for ID: " + id);
        // Reset the current value
        currentWorkoutLog.setValue(null);
        
        // List all logs for debugging
        new Thread(() -> {
            dbHelper.listAllWorkoutLogs();
        }).start();
        
        // Load data immediately in a background thread
        new Thread(() -> {
            try {
                WorkoutLog log = dbHelper.getWorkoutLogById(id);
                android.util.Log.d("WorkoutLogViewModel", "Retrieved workout log: " + (log != null ? "not null" : "null"));
                if (log != null) {
                    android.util.Log.d("WorkoutLogViewModel", "Setting workout log data - ID: " + log.getId() 
                        + ", Type: " + log.getWorkoutType() 
                        + ", Date: " + log.getDate());
                    // Post the result to the main thread
                    new Handler(Looper.getMainLooper()).post(() -> {
                        currentWorkoutLog.setValue(log);
                    });
                } else {
                    android.util.Log.e("WorkoutLogViewModel", "No workout log found for ID: " + id);
                    // Post null to the main thread
                    new Handler(Looper.getMainLooper()).post(() -> {
                        currentWorkoutLog.setValue(null);
                    });
                }
            } catch (Exception e) {
                android.util.Log.e("WorkoutLogViewModel", "Error getting workout log", e);
                // Post null to the main thread in case of error
                new Handler(Looper.getMainLooper()).post(() -> {
                    currentWorkoutLog.setValue(null);
                });
            }
        }).start();
        
        return currentWorkoutLog;
    }

    public void setFilter(String filter) {
        this.currentFilter = filter;
        loadData();
    }

    public void insertWorkoutLog(WorkoutLog log) {
        new Thread(() -> {
            dbHelper.insertWorkoutLog(log);
            loadData();
        }).start();
    }

    private void checkAndInsertDummyData() {
        new Thread(() -> {
            // Check if database is empty
            List<WorkoutLog> existingLogs = dbHelper.getAllLogs();
            if (existingLogs.isEmpty()) {
                android.util.Log.d("WorkoutLogViewModel", "Database is empty, inserting dummy data");
                insertDummyData();
            } else {
                android.util.Log.d("WorkoutLogViewModel", "Database already has " + existingLogs.size() + " logs");
            }
        }).start();
    }

    private void insertDummyData() {
        try {
            // Clear existing data first
            dbHelper.clearAllData();
            android.util.Log.d("WorkoutLogViewModel", "Cleared existing data");

            List<WorkoutLog> dummyLogs = new ArrayList<>();
            
            // Tuần 1 - Tháng 5
            dummyLogs.add(new WorkoutLog(0, "2025-05-01", "Chạy bộ", 45, 450, 6000, "Chạy bộ buổi sáng tại công viên"));
            dummyLogs.add(new WorkoutLog(0, "2025-05-02", "Yoga", 60, 200, null, "Yoga tại nhà với video hướng dẫn"));
            dummyLogs.add(new WorkoutLog(0, "2025-05-03", "Bơi lội", 90, 600, null, "Bơi tại hồ bơi công cộng"));
            dummyLogs.add(new WorkoutLog(0, "2025-05-04", "Đạp xe", 120, 800, 8000, "Đạp xe dọc bờ sông"));
            dummyLogs.add(new WorkoutLog(0, "2025-05-05", "Nhảy dây", 30, 350, 3000, "Nhảy dây tại nhà"));
            
            // Tuần 2 - Tháng 5
            dummyLogs.add(new WorkoutLog(0, "2025-05-08", "Chạy bộ", 60, 600, 8000, "Chạy marathon tập luyện"));
            dummyLogs.add(new WorkoutLog(0, "2025-05-09", "Yoga", 45, 150, null, "Yoga buổi tối thư giãn"));
            dummyLogs.add(new WorkoutLog(0, "2025-05-10", "Bơi lội", 60, 400, null, "Bơi tự do và bơi ếch"));
            dummyLogs.add(new WorkoutLog(0, "2025-05-11", "Đạp xe", 90, 600, 6000, "Đạp xe leo dốc"));
            dummyLogs.add(new WorkoutLog(0, "2025-05-12", "Nhảy dây", 45, 500, 4500, "Nhảy dây cường độ cao"));
            
            // Tuần 3 - Tháng 5
            dummyLogs.add(new WorkoutLog(0, "2025-05-15", "Chạy bộ", 30, 300, 4000, "Chạy bộ nhẹ nhàng"));
            dummyLogs.add(new WorkoutLog(0, "2025-05-16", "Yoga", 90, 300, null, "Yoga nâng cao với huấn luyện viên"));
            dummyLogs.add(new WorkoutLog(0, "2025-05-17", "Bơi lội", 120, 800, null, "Bơi thi đấu tập luyện"));
            dummyLogs.add(new WorkoutLog(0, "2025-05-18", "Đạp xe", 60, 400, 5000, "Đạp xe trong công viên"));
            dummyLogs.add(new WorkoutLog(0, "2025-05-19", "Nhảy dây", 20, 250, 2000, "Nhảy dây khởi động"));
            
            // Tuần 4 - Tháng 5
            dummyLogs.add(new WorkoutLog(0, "2025-05-22", "Chạy bộ", 45, 450, 6000, "Chạy bộ với bạn bè"));
            dummyLogs.add(new WorkoutLog(0, "2025-05-23", "Yoga", 60, 200, null, "Yoga thiền định"));
            dummyLogs.add(new WorkoutLog(0, "2025-05-24", "Bơi lội", 90, 600, null, "Bơi bướm và bơi ngửa"));
            dummyLogs.add(new WorkoutLog(0, "2025-05-25", "Đạp xe", 120, 800, 8000, "Đạp xe đường dài"));
            dummyLogs.add(new WorkoutLog(0, "2025-05-26", "Nhảy dây", 40, 400, 3500, "Nhảy dây kết hợp bài tập khác"));
            
            // Tuần 1 - Tháng 6
            dummyLogs.add(new WorkoutLog(0, "2025-06-01", "Chạy bộ", 60, 600, 8000, "Chạy bộ buổi sáng sớm"));
            dummyLogs.add(new WorkoutLog(0, "2025-06-02", "Yoga", 45, 150, null, "Yoga flow"));
            dummyLogs.add(new WorkoutLog(0, "2025-06-03", "Bơi lội", 60, 400, null, "Bơi tự do"));
            dummyLogs.add(new WorkoutLog(0, "2025-06-04", "Đạp xe", 90, 600, 6000, "Đạp xe địa hình"));
            dummyLogs.add(new WorkoutLog(0, "2025-06-05", "Nhảy dây", 30, 350, 3000, "Nhảy dây cường độ trung bình"));

            // Insert all dummy data
            for (WorkoutLog log : dummyLogs) {
                dbHelper.insertWorkoutLog(log);
                android.util.Log.d("WorkoutLogViewModel", "Inserted workout log - Type: " + log.getWorkoutType() + ", Date: " + log.getDate());
            }

            android.util.Log.d("WorkoutLogViewModel", "Successfully inserted " + dummyLogs.size() + " dummy logs");
            
            // Verify data was inserted
            List<WorkoutLog> insertedLogs = dbHelper.getAllLogs();
            android.util.Log.d("WorkoutLogViewModel", "Verified " + insertedLogs.size() + " logs in database");

        } catch (Exception e) {
            android.util.Log.e("WorkoutLogViewModel", "Error inserting dummy data", e);
        }
    }

    private List<String> generateLabels(List<WorkoutLog> logs) {
        List<String> labels = new ArrayList<>();
        if (logs.isEmpty()) return labels;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        try {
            switch (currentFilter) {
                case "Hôm nay":
                case "Tuần này":
                    // Labels: Thứ 2 -> Chủ nhật
                    String[] weekDays = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
                    for (String day : weekDays) {
                        labels.add(day);
                    }
                    break;

                case "Tháng này":
                    // Labels: Tuần 1 -> Tuần 4
                    for (int i = 1; i <= 4; i++) {
                        labels.add("Tuần " + i);
                    }
                    break;

                case "Năm nay":
                    // Labels: Tháng 1 -> Tháng 12
                    for (int i = 1; i <= 12; i++) {
                        labels.add("Tháng " + i);
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return labels;
    }

    private Map<String, List<WorkoutLog>> groupLogsByPeriod(List<WorkoutLog> logs) {
        Map<String, List<WorkoutLog>> groupedLogs = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        try {
            // Khởi tạo tất cả các nhóm với list rỗng
            switch (currentFilter) {
                case "Hôm nay":
                case "Tuần này":
                    // Khởi tạo cho 7 ngày trong tuần
                    for (int i = 1; i <= 7; i++) {
                        groupedLogs.put(String.valueOf(i), new ArrayList<>());
                    }
                    break;
                case "Tháng này":
                    // Khởi tạo cho 4 tuần trong tháng
                    for (int i = 1; i <= 4; i++) {
                        groupedLogs.put(String.valueOf(i), new ArrayList<>());
                    }
                    break;
                case "Năm nay":
                    // Khởi tạo cho 12 tháng trong năm
                    for (int i = 1; i <= 12; i++) {
                        groupedLogs.put(String.valueOf(i), new ArrayList<>());
                    }
                    break;
            }

            // Phân nhóm dữ liệu
            for (WorkoutLog log : logs) {
                calendar.setTime(dateFormat.parse(log.getDate()));
                String key;

                switch (currentFilter) {
                    case "Hôm nay":
                    case "Tuần này":
                        // Group by day of week (0 = Sunday, 1 = Monday, etc.)
                        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                        key = String.valueOf(dayOfWeek == 1 ? 7 : dayOfWeek - 1); // Convert to Monday-based
                        break;

                    case "Tháng này":
                        // Group by week of month
                        int weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH);
                        key = String.valueOf(weekOfMonth);
                        break;

                    case "Năm nay":
                        // Group by month
                        key = String.valueOf(calendar.get(Calendar.MONTH) + 1);
                        break;

                    default:
                        key = log.getDate();
                }

                if (groupedLogs.containsKey(key)) {
                    groupedLogs.get(key).add(log);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return groupedLogs;
    }

    public void loadData() {
        new Thread(() -> {
            List<WorkoutLog> filteredLogs = dbHelper.getLogsByFilter(currentFilter);
            Map<String, List<WorkoutLog>> groupedLogs = groupLogsByPeriod(filteredLogs);
            List<String> labels = generateLabels(filteredLogs);
            
            // Update chart labels
            new Handler(Looper.getMainLooper()).post(() -> chartLabels.setValue(labels));

            // Update summary with appropriate filter text
            int totalSteps = 0, totalCalories = 0, totalHours = 0;
            for (WorkoutLog log : filteredLogs) {
                totalCalories += log.getCalories();
                totalHours += log.getDurationMinutes() / 60;
                if (log.getSteps() != null) totalSteps += log.getSteps();
            }
            String summaryText = String.format("%s: %d bước, %d giờ tập, %d calo",
                    currentFilter, totalSteps, totalHours, totalCalories);
            new Handler(Looper.getMainLooper()).post(() -> summary.setValue(summaryText));

            // Update steps chart
            List<Entry> stepEntries = new ArrayList<>();
            for (int i = 0; i < labels.size(); i++) {
                String key = String.valueOf(i + 1); // Convert to 1-based index
                List<WorkoutLog> periodLogs = groupedLogs.getOrDefault(key, new ArrayList<>());
                int totalStepsForPeriod = 0;
                for (WorkoutLog log : periodLogs) {
                    if (log.getSteps() != null) {
                        totalStepsForPeriod += log.getSteps();
                    }
                }
                stepEntries.add(new Entry(i, totalStepsForPeriod));
            }
            new Handler(Looper.getMainLooper()).post(() -> stepsData.setValue(stepEntries));

            // Update calories chart
            List<BarEntry> calorieEntries = new ArrayList<>();
            for (int i = 0; i < labels.size(); i++) {
                String key = String.valueOf(i + 1); // Convert to 1-based index
                List<WorkoutLog> periodLogs = groupedLogs.getOrDefault(key, new ArrayList<>());
                int totalCaloriesForPeriod = 0;
                for (WorkoutLog log : periodLogs) {
                    totalCaloriesForPeriod += log.getCalories();
                }
                calorieEntries.add(new BarEntry(i, totalCaloriesForPeriod));
            }
            new Handler(Looper.getMainLooper()).post(() -> caloriesData.setValue(calorieEntries));

            // Update workout type chart
            Map<String, Integer> workoutCounts = new HashMap<>();
            for (WorkoutLog log : filteredLogs) {
                workoutCounts.merge(log.getWorkoutType(), 1, Integer::sum);
            }
            List<PieEntry> pieEntries = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : workoutCounts.entrySet()) {
                pieEntries.add(new PieEntry(entry.getValue(), entry.getKey()));
            }
            new Handler(Looper.getMainLooper()).post(() -> workoutTypeData.setValue(pieEntries));

            // Update workout logs
            new Handler(Looper.getMainLooper()).post(() -> workoutLogs.setValue(filteredLogs));
        }).start();
    }

    public void updateWorkoutLog(WorkoutLog log) {
        new Thread(() -> {
            dbHelper.updateWorkoutLog(log);
            loadData();
        }).start();
    }

    public void deleteWorkoutLog(int id) {
        new Thread(() -> {
            dbHelper.deleteWorkoutLog(id);
            loadData();
        }).start();
    }
}