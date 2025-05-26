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
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;

import com.example.healthtrackerapp.R;
import com.example.healthtrackerapp.adapter.ImageAdapter;
import com.example.healthtrackerapp.viewmodel.ImageViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ImageFragment extends Fragment {
    private static final int REQUEST_IMAGE_PICK = 1001;

    private static final int REQUEST_PERMISSIONS = 123;

    private static final int REQUEST_FILE_PICK = 2001; // mới


    ImageViewModel imageViewModel = new ImageViewModel();

    private RecyclerView recyclerView;
    private FloatingActionButton uploadBtn;

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Cho phép chọn nhiều ảnh
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), REQUEST_IMAGE_PICK);
    }

    public ImageFragment() {
        super(R.layout.fragment_upload_images);
    }

//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        uploadBtn = view.findViewById(R.id.btn_upload);
//
//        uploadBtn.setOnClickListener(v -> {
//            checkAndRequestPermissions();
//        });
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload_images, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        uploadBtn = view.findViewById(R.id.btn_upload);

        uploadBtn.setOnClickListener(v -> {
            checkAndRequestPermissions();
        });

        loadImagesFromFirestore();

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
                    imageViewModel.uploadImageToCloudinary(requireContext(), fileUri);
                }
            } else if (data.getData() != null) {
                Uri fileUri = data.getData();
                imageViewModel.uploadImageToCloudinary(requireContext(), fileUri);
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

    private void loadImagesFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("uploads")  // hoặc "files" nếu bạn đã đổi tên
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> fileUrls = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        String url = doc.getString("url");
                        if (url != null) {
                            fileUrls.add(url);  // Có thể là ảnh hoặc PDF
                        }
                    }

                    recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                    recyclerView.setAdapter(new ImageAdapter(getContext(), fileUrls));
                });
    }


    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"image/*", "application/pdf"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // nếu muốn chọn nhiều
        startActivityForResult(Intent.createChooser(intent, "Select files"), REQUEST_FILE_PICK);
    }
}