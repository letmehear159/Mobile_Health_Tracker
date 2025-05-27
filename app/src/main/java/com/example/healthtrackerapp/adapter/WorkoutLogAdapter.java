package com.example.healthtrackerapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthtrackerapp.R;
import com.example.healthtrackerapp.model.WorkoutLog;
import com.example.healthtrackerapp.view.activity.EditWorkoutLogActivity;

import java.util.List;

public class WorkoutLogAdapter extends RecyclerView.Adapter<WorkoutLogAdapter.ViewHolder> {
    private List<WorkoutLog> workoutLogs;
    private Context context;

    public WorkoutLogAdapter(Context context, List<WorkoutLog> workoutLogs) {
        this.context = context;
        this.workoutLogs = workoutLogs;
    }

    public void setWorkoutLogs(List<WorkoutLog> logs) {
        this.workoutLogs = logs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutLog log = workoutLogs.get(position);
        
        // Set workout type icon and text
        holder.workoutTypeIcon.setImageResource(getWorkoutTypeIcon(log.getWorkoutType()));
        holder.workoutTypeText.setText(log.getWorkoutType());
        holder.dateText.setText(log.getDate());
        
        // Set duration, steps, and calories
        holder.durationText.setText(String.format("%d phút", log.getDurationMinutes()));
        if (log.getSteps() != null) {
            holder.stepsText.setVisibility(View.VISIBLE);
            holder.stepsText.setText(String.format("%d bước", log.getSteps()));
        } else {
            holder.stepsText.setVisibility(View.GONE);
        }
        holder.caloriesText.setText(String.format("%d calo", log.getCalories()));
        
        // Set notes if available
        if (log.getNotes() != null && !log.getNotes().isEmpty()) {
            holder.notesText.setVisibility(View.VISIBLE);
            holder.notesText.setText(log.getNotes());
        } else {
            holder.notesText.setVisibility(View.GONE);
        }

        // Add click listener to the card
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditWorkoutLogActivity.class);
            intent.putExtra("workout_log_id", log.getId());
            android.util.Log.d("WorkoutLogAdapter", "Starting EditWorkoutLogActivity with ID: " + log.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return workoutLogs.size();
    }

    private int getWorkoutTypeIcon(String workoutType) {
        // Use the same icon for all workout types
        return android.R.drawable.ic_menu_agenda;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView workoutTypeIcon;
        TextView workoutTypeText;
        TextView dateText;
        TextView durationText;
        TextView stepsText;
        TextView caloriesText;
        TextView notesText;

        ViewHolder(View itemView) {
            super(itemView);
            workoutTypeIcon = itemView.findViewById(R.id.workoutTypeIcon);
            workoutTypeText = itemView.findViewById(R.id.workoutTypeText);
            dateText = itemView.findViewById(R.id.dateText);
            durationText = itemView.findViewById(R.id.durationText);
            stepsText = itemView.findViewById(R.id.stepsText);
            caloriesText = itemView.findViewById(R.id.caloriesText);
            notesText = itemView.findViewById(R.id.notesText);
        }
    }
}