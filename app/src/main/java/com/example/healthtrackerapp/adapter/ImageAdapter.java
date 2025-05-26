package com.example.healthtrackerapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.healthtrackerapp.R;
import com.example.healthtrackerapp.model.FileItem;
import com.example.healthtrackerapp.view.activity.FullScreenImageActivity;

import java.util.Date;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private List<FileItem> items;
    private Context context;

    public ImageAdapter(Context context, List<FileItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FileItem item = items.get(position);
        String fileUrl = item.getUrl();

        // Hiển thị tên file và thời gian
        holder.fileNameText.setText(item.getName() != null ? item.getName() : "Không rõ tên");
        String formattedDate = DateFormat.format("dd/MM/yyyy HH:mm", new Date(item.getTimestampMillis())).toString();
        holder.fileDateText.setText(formattedDate);

        if (fileUrl.endsWith(".pdf")) {
            holder.imageView.setImageResource(R.drawable.pdf); // cần thêm icon PDF vào drawable
            holder.imageView.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(android.net.Uri.parse(fileUrl), "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            });
        } else {
            Glide.with(context).load(fileUrl).into(holder.imageView);
            holder.imageView.setOnClickListener(v -> {
                Intent intent = new Intent(context, FullScreenImageActivity.class);
                intent.putExtra("imageUrl", fileUrl);
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView fileNameText, fileDateText;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img_view);
            fileNameText = itemView.findViewById(R.id.file_name);
            fileDateText = itemView.findViewById(R.id.file_date);
        }
    }
}
