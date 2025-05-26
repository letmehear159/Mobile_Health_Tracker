package com.example.healthtrackerapp.view.fragment;

import com.example.healthtrackerapp.model.Appointment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthtrackerapp.R;
import com.example.healthtrackerapp.adapter.AppointmentAdapter;
import com.example.healthtrackerapp.view.activity.CreateAppointmentActivity;
import com.example.healthtrackerapp.viewmodel.AppointmentViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AppointmentFragment extends Fragment {

    private AppointmentViewModel viewModel;
    private AppointmentAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_appointment, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.recyclerAppointments);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AppointmentAdapter(new AppointmentAdapter.OnItemClickListener() {
            @Override
            public void onEdit(Appointment appointment) {
                Intent intent = new Intent(getActivity(), CreateAppointmentActivity.class);
                intent.putExtra("edit_id", appointment.id);
                intent.putExtra("doctor", appointment.doctorName);
                intent.putExtra("date", appointment.date);
                intent.putExtra("time", appointment.time);
                intent.putExtra("location", appointment.location);
                intent.putExtra("ticket", appointment.ticketCode);
                startActivity(intent);
            }

            @Override
            public void onDelete(Appointment appointment) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Xác nhận")
                        .setMessage("Bạn có chắc muốn xóa lịch hẹn này?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            viewModel.delete(appointment);
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });

        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = root.findViewById(R.id.fabAddAppointment);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateAppointmentActivity.class);
            startActivity(intent);
        });

        viewModel = new ViewModelProvider(this).get(AppointmentViewModel.class);
        viewModel.getAllAppointments().observe(getViewLifecycleOwner(), appointments -> {
            adapter.setAppointments(appointments);
        });

        return root;
    }
}
