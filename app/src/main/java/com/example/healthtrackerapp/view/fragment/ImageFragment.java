package com.example.healthtrackerapp.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;

import com.example.healthtrackerapp.R;
import com.example.healthtrackerapp.adapter.ImageAdapter;
import com.example.healthtrackerapp.viewmodel.ImageViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ImageFragment extends Fragment {
    private static final int REQUEST_PERMISSIONS = 123;

    private static final int REQUEST_FILE_PICK = 2001; // mới
    private ImageViewModel imageViewModel;

    private RecyclerView recyclerView;
    private FloatingActionButton uploadBtn;


    public ImageFragment() {
        super(R.layout.fragment_upload_images);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload_images, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        uploadBtn = view.findViewById(R.id.btn_upload);

        uploadBtn.setOnClickListener(v -> {
            checkAndRequestPermissions();
        });

        imageViewModel = new ViewModelProvider(this).get(ImageViewModel.class); // gán vào field


        imageViewModel.getFileItems().observe(getViewLifecycleOwner(), urls -> {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            recyclerView.setAdapter(new ImageAdapter(getContext(), urls));
        });

        imageViewModel.loadImagesFromFirestore();


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_FILE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri fileUri = data.getClipData().getItemAt(i).getUri();
                    promptForFileNameAndUpload(fileUri); // <-- dialog nhập tên cho từng file
                }
            } else if (data.getData() != null) {
                Uri fileUri = data.getData();
                promptForFileNameAndUpload(fileUri); // <-- dialog nhập tên
            }
        }
    }



    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_PERMISSIONS);
            } else {
                openFilePicker();
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);
            } else {
                openFilePicker();
            }
        }
    }


    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"image/*", "application/pdf", "video/*"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // nếu muốn chọn nhiều
        startActivityForResult(Intent.createChooser(intent, "Select files"), REQUEST_FILE_PICK);
    }


    private void promptForFileNameAndUpload(Uri fileUri) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Nhập tên file");

        final android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setHint("VD: Ket_qua_xet_nghiem.pdf");
        builder.setView(input);

        builder.setPositiveButton("Tải lên", (dialog, which) -> {
            String customName = input.getText().toString().trim();
            imageViewModel.uploadFileToCloudinary(requireContext(), fileUri, customName);
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

}