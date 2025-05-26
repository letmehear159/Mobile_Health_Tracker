package com.example.healthtrackerapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthtrackerapp.R;
import com.example.healthtrackerapp.model.Appointment;

import java.util.ArrayList;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private List<Appointment> appointmentList = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEdit(Appointment appointment);
        void onDelete(Appointment appointment);
    }

    public AppointmentAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointmentList = appointments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment current = appointmentList.get(position);
        holder.tvDoctor.setText("Bác sĩ: " + current.doctorName);
        holder.tvDateTime.setText("Ngày: " + current.date + " - " + current.time);
        holder.tvLocation.setText("Địa điểm: " + current.location);
        holder.tvTicket.setText("Mã vé: " + current.ticketCode);

        // 👉 Gắn sự kiện cho nút sửa
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(current));

        // 👉 Gắn sự kiện cho nút xóa
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(current));
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvDoctor, tvDateTime, tvLocation, tvTicket;
        ImageView btnEdit, btnDelete;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDoctor = itemView.findViewById(R.id.tvDoctorName);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvTicket = itemView.findViewById(R.id.tvTicketCode);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
