package com.example.healthtrackerapp.repository;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.healthtrackerapp.manager.CloudinaryManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryRepository {

    public void uploadFile(Context context, Uri fileUri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();
        MediaManager.get().upload(fileUri)
                .unsigned("HealthCheckerApp")
                .option("folder", "health_images/" + userId)
                .option("resource_type", "auto")
                .option("public_id", "file_" + System.currentTimeMillis())
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d("Cloudinary", "Start upload");
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        String format = (String) resultData.get("format"); // e.g., pdf, jpg, png
                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        Map<String, Object> fileData = new HashMap<>();
                        fileData.put("url", url);
                        fileData.put("type", format); // "pdf", "jpg", etc
                        fileData.put("timestamp", FieldValue.serverTimestamp());

                        db.collection("users")
                                .document(userId)
                                .collection("uploads") // dùng tên chung
                                .add(fileData)
                                .addOnSuccessListener(docRef -> Log.d("Cloudinary", "URL saved to Firestore"))
                                .addOnFailureListener(e -> Log.e("Cloudinary", "Failed to save URL", e));
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e("Cloudinary", "Upload error: " + error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                    }
                }).dispatch(context);
    }
}
