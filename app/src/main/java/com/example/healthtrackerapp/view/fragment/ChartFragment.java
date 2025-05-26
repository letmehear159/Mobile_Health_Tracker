package com.example.healthtrackerapp.view.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthtrackerapp.view.activity.AddWorkoutLogActivity;
import com.example.healthtrackerapp.R;
import com.example.healthtrackerapp.adapter.WorkoutLogAdapter;
import com.example.healthtrackerapp.viewmodel.WorkoutLogViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class ChartFragment extends Fragment {
    private WorkoutLogViewModel viewModel;
    private WorkoutLogAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(WorkoutLogViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_daily_health, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Setup filters
        setupFilters(view);
        
        // Setup RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.workoutLogsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new WorkoutLogAdapter(requireContext(), new ArrayList<>());
        recyclerView.setAdapter(adapter);
        
        // Setup charts configuration
        setupStepsChart(view);
        setupCaloriesChart(view);
        setupWorkoutTypeChart(view);
        
        // Setup add button
        MaterialButton addButton = view.findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddWorkoutLogActivity.class);
            startActivity(intent);
        });
        
        // Observe data from ViewModel
        observeViewModelData(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadData();
    }

    private void setupFilters(View view) {
        String[] filterOptions = new String[]{"Hôm nay", "Tuần này", "Tháng này", "Năm nay"};
        
        TextView summaryFilter = view.findViewById(R.id.summaryFilter);
        setupFilterTextView(summaryFilter, filterOptions, filter -> {
            updateSummaryData(filter);
            String chartFilter = filter.equals("Hôm nay") ? "Tuần này" : filter;
            updateChartsData(chartFilter);
            updateWorkoutLogsData(filter);
        });
    }

    private void setupFilterTextView(TextView textView, String[] options, OnFilterChangeListener listener) {
        textView.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), textView);
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

    private void observeViewModelData(View view) {
        viewModel.getSummary().observe(getViewLifecycleOwner(), summary -> {
            String[] parts = summary.split(": ")[1].split(", ");
            int steps = Integer.parseInt(parts[0].split(" ")[0]);
            int hours = Integer.parseInt(parts[1].split(" ")[0]);
            int calories = Integer.parseInt(parts[2].split(" ")[0]);

            TextView stepsText = view.findViewById(R.id.stepsSummaryText);
            TextView hoursText = view.findViewById(R.id.hoursSummaryText);
            TextView caloriesText = view.findViewById(R.id.caloriesSummaryText);
            TextView periodText = view.findViewById(R.id.summaryPeriodText);

            stepsText.setText(String.valueOf(steps));
            hoursText.setText(String.valueOf(hours));
            caloriesText.setText(String.valueOf(calories));
            periodText.setText(summary.split(":")[0] + ":");
        });

        viewModel.getStepsData().observe(getViewLifecycleOwner(), entries -> {
            LineChart stepsChart = view.findViewById(R.id.stepsChart);
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

        viewModel.getCaloriesData().observe(getViewLifecycleOwner(), entries -> {
            BarChart caloriesChart = view.findViewById(R.id.caloriesChart);
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

        viewModel.getWorkoutTypeData().observe(getViewLifecycleOwner(), entries -> {
            PieChart workoutTypeChart = view.findViewById(R.id.workoutTypeChart);
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

        viewModel.getWorkoutLogs().observe(getViewLifecycleOwner(), logs -> {
            adapter.setWorkoutLogs(logs);
        });

        viewModel.getChartLabels().observe(getViewLifecycleOwner(), labels -> {
            if (labels != null && !labels.isEmpty()) {
                LineChart stepsChart = view.findViewById(R.id.stepsChart);
                BarChart caloriesChart = view.findViewById(R.id.caloriesChart);
                
                stepsChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                caloriesChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                
                stepsChart.invalidate();
                caloriesChart.invalidate();
            }
        });
    }

    private void setupStepsChart(View view) {
        LineChart stepsChart = view.findViewById(R.id.stepsChart);
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
    }

    private void setupCaloriesChart(View view) {
        BarChart caloriesChart = view.findViewById(R.id.caloriesChart);
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
    }

    private void setupWorkoutTypeChart(View view) {
        PieChart workoutTypeChart = view.findViewById(R.id.workoutTypeChart);
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
}