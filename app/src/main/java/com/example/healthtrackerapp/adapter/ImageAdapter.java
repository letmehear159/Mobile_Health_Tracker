package com.example.healthtrackerapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

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
        String fileName = item.getName() != null ? item.getName() : "Không rõ tên";

        // Hiển thị tên và ngày
        holder.fileNameText.setText(fileName);
        String formattedDate = DateFormat.format("dd/MM/yyyy HH:mm", new Date(item.getTimestampMillis())).toString();
        holder.fileDateText.setText(formattedDate);

        // Xác định loại file
        if (fileUrl.endsWith(".pdf")) {
            holder.imageView.setVisibility(View.VISIBLE);
            holder.imageView.setImageResource(R.drawable.pdf);
            holder.imageView.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(fileUrl), "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            });

        } else if (fileUrl.endsWith(".mp4") || fileUrl.endsWith(".mov") || fileUrl.endsWith(".3gp")) {
            holder.imageView.setVisibility(View.VISIBLE);

            // Hiển thị thumbnail video bằng Glide (nó lấy khung đầu)
            Glide.with(context)
                    .load(fileUrl)
                    .thumbnail(0.1f) // lấy khung đầu
                    .into(holder.imageView);

            holder.imageView.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(fileUrl), "video/*");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            });

        } else {
            // Ảnh
            holder.imageView.setVisibility(View.VISIBLE);
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
        public ImageView imageView;
        public TextView fileNameText;
        public TextView fileDateText;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img_view);
            fileNameText = itemView.findViewById(R.id.file_name);
            fileDateText = itemView.findViewById(R.id.file_date);
        }

    }
}
