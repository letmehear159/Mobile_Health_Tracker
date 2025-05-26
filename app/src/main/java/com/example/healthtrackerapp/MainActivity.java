package com.example.healthtrackerapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthtrackerapp.viewmodel.WorkoutLogViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.button.MaterialButton;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private WorkoutLogViewModel viewModel;
    private WorkoutLogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_health);

        viewModel = new ViewModelProvider(this).get(WorkoutLogViewModel.class);

        // Setup filters
        setupFilters();

        // Setup RecyclerView
        RecyclerView recyclerView = findViewById(R.id.workoutLogsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WorkoutLogAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Setup charts configuration
        setupStepsChart();
        setupCaloriesChart();
        setupWorkoutTypeChart();

        // Observe data from ViewModel
        viewModel.getSummary().observe(this, summary -> {
            // Parse the summary string to get individual values
            String[] parts = summary.split(": ")[1].split(", ");
            int steps = Integer.parseInt(parts[0].split(" ")[0]);
            int hours = Integer.parseInt(parts[1].split(" ")[0]);
            int calories = Integer.parseInt(parts[2].split(" ")[0]);

            // Update individual TextViews
            TextView stepsText = findViewById(R.id.stepsSummaryText);
            TextView hoursText = findViewById(R.id.hoursSummaryText);
            TextView caloriesText = findViewById(R.id.caloriesSummaryText);
            TextView periodText = findViewById(R.id.summaryPeriodText);

            stepsText.setText(String.valueOf(steps));
            hoursText.setText(String.valueOf(hours));
            caloriesText.setText(String.valueOf(calories));
            periodText.setText(summary.split(":")[0] + ":");
        });

        viewModel.getStepsData().observe(this, entries -> {
            LineChart stepsChart = findViewById(R.id.stepsChart);
            if (entries != null && !entries.isEmpty()) {
                LineDataSet dataSet = new LineDataSet(entries, "Bước mỗi ngày");
                dataSet.setColor(Color.parseColor("#92A3FD"));
                dataSet.setValueTextColor(Color.parseColor("#333333"));
                dataSet.setCircleColor(Color.parseColor("#92A3FD"));
                dataSet.setCircleRadius(4f);
                dataSet.setLineWidth(2f);
                dataSet.setDrawValues(true);
                dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                dataSet.setDrawFilled(true);
                dataSet.setFillColor(Color.parseColor("#92A3FD"));
                dataSet.setFillAlpha(30);

                LineData lineData = new LineData(dataSet);
                stepsChart.setData(lineData);
                stepsChart.invalidate();
            }
        });

        viewModel.getCaloriesData().observe(this, entries -> {
            BarChart caloriesChart = findViewById(R.id.caloriesChart);
            if (entries != null && !entries.isEmpty()) {
                BarDataSet dataSet = new BarDataSet(entries, "Calo mỗi ngày");
                dataSet.setColor(Color.parseColor("#9DCEFF"));
                dataSet.setValueTextColor(Color.parseColor("#333333"));
                dataSet.setValueTextSize(12f);

                BarData barData = new BarData(dataSet);
                caloriesChart.setData(barData);
                caloriesChart.invalidate();
            }
        });

        viewModel.getWorkoutTypeData().observe(this, entries -> {
            PieChart workoutTypeChart = findViewById(R.id.workoutTypeChart);
            if (entries != null && !entries.isEmpty()) {
                PieDataSet dataSet = new PieDataSet(entries, "Loại bài tập");
                int[] colors = new int[] {
                    Color.parseColor("#92A3FD"),
                    Color.parseColor("#9DCEFF"),
                    Color.parseColor("#B4E4FF")
                };
                dataSet.setColors(colors);
                dataSet.setValueTextColor(Color.WHITE);
                dataSet.setValueTextSize(14f);
                dataSet.setValueLineColor(Color.WHITE);
                dataSet.setYValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);

                PieData pieData = new PieData(dataSet);
                workoutTypeChart.setData(pieData);
                workoutTypeChart.invalidate();
            }
        });

        viewModel.getWorkoutLogs().observe(this, logs -> {
            adapter.setWorkoutLogs(logs);
        });

        // Add button
        MaterialButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddWorkoutLogActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        android.util.Log.d("MainActivity", "onResume: Reloading data");
        // Reload all data when returning to this activity
        viewModel.loadData();
    }

    private void setupFilters() {
        String[] filterOptions = new String[]{"Hôm nay", "Tuần này", "Tháng này", "Năm nay"};
        
        // Setup Summary Filter
        TextView summaryFilter = findViewById(R.id.summaryFilter);
        setupFilterTextView(summaryFilter, filterOptions, filter -> {
            // Update summary with exact filter
            updateSummaryData(filter);
            // Update charts with week filter if today is selected
            String chartFilter = filter.equals("Hôm nay") ? "Tuần này" : filter;
            updateChartsData(chartFilter);
            // Update workout logs with exact filter
            updateWorkoutLogsData(filter);
        });


    }

    private void setupFilterTextView(TextView textView, String[] options, OnFilterChangeListener listener) {
        textView.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, textView);
            for (String option : options) {
                popup.getMenu().add(option);
            }
            
            popup.setOnMenuItemClickListener(item -> {
                String selectedFilter = item.getTitle().toString();
                textView.setText(selectedFilter);
                listener.onFilterChanged(selectedFilter);
                return true;
            });
            
            popup.show();
        });
    }

    private interface OnFilterChangeListener {
        void onFilterChanged(String filter);
    }

    private void updateChartsData(String filter) {
        viewModel.setFilter(filter);
    }

    private void updateSummaryData(String filter) {
        viewModel.setFilter(filter);
    }

    private void updateWorkoutLogsData(String filter) {
        viewModel.setFilter(filter);
    }

    private void setupStepsChart() {
        LineChart stepsChart = findViewById(R.id.stepsChart);
        stepsChart.getDescription().setEnabled(true);
        stepsChart.getDescription().setText("Số bước đi mỗi ngày");
        stepsChart.getDescription().setTextColor(Color.parseColor("#333333"));
        stepsChart.setDrawGridBackground(false);
        stepsChart.setDrawBorders(false);
        stepsChart.getAxisRight().setEnabled(false);
        stepsChart.getXAxis().setDrawGridLines(false);
        stepsChart.getXAxis().setTextColor(Color.parseColor("#666666"));
        stepsChart.getAxisLeft().setTextColor(Color.parseColor("#666666"));
        stepsChart.getLegend().setEnabled(true);
        stepsChart.getLegend().setTextColor(Color.parseColor("#333333"));
        stepsChart.setBackgroundColor(Color.WHITE);
        stepsChart.animateX(1000);

        // Observe chart labels
        viewModel.getChartLabels().observe(this, labels -> {
            if (labels != null && !labels.isEmpty()) {
                stepsChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                stepsChart.invalidate();
            }
        });
    }

    private void setupCaloriesChart() {
        BarChart caloriesChart = findViewById(R.id.caloriesChart);
        caloriesChart.getDescription().setEnabled(true);
        caloriesChart.getDescription().setText("Calories đốt cháy mỗi ngày");
        caloriesChart.getDescription().setTextColor(Color.parseColor("#333333"));
        caloriesChart.setDrawGridBackground(false);
        caloriesChart.setDrawBorders(false);
        caloriesChart.getAxisRight().setEnabled(false);
        caloriesChart.getXAxis().setDrawGridLines(false);
        caloriesChart.getXAxis().setTextColor(Color.parseColor("#666666"));
        caloriesChart.getAxisLeft().setTextColor(Color.parseColor("#666666"));
        caloriesChart.getLegend().setEnabled(true);
        caloriesChart.getLegend().setTextColor(Color.parseColor("#333333"));
        caloriesChart.setBackgroundColor(Color.WHITE);
        caloriesChart.animateY(1000);

        // Observe chart labels
        viewModel.getChartLabels().observe(this, labels -> {
            if (labels != null && !labels.isEmpty()) {
                caloriesChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                caloriesChart.invalidate();
            }
        });
    }

    private void setupWorkoutTypeChart() {
        PieChart workoutTypeChart = findViewById(R.id.workoutTypeChart);
        workoutTypeChart.getDescription().setEnabled(true);
        workoutTypeChart.getDescription().setText("Phân bố loại bài tập");
        workoutTypeChart.getDescription().setTextColor(Color.parseColor("#333333"));
        workoutTypeChart.setDrawHoleEnabled(true);
        workoutTypeChart.setHoleRadius(50f);
        workoutTypeChart.setTransparentCircleRadius(55f);
        workoutTypeChart.setRotationAngle(0);
        workoutTypeChart.setRotationEnabled(true);
        workoutTypeChart.getLegend().setEnabled(true);
        workoutTypeChart.getLegend().setTextColor(Color.parseColor("#333333"));
        workoutTypeChart.setBackgroundColor(Color.WHITE);
        workoutTypeChart.animateY(1000);
    }
}