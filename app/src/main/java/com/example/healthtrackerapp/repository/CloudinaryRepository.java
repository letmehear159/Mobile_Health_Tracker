package com.example.healthtrackerapp.repository;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.healthtrackerapp.model.FileItem;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudinaryRepository {

    public interface LoadCallbackInterface {
        void onUploadSuccess(List<FileItem> items);

        void onLoadFailure(Exception e);
    }

    public void uploadFile(Context context, Uri fileUri, String customFileName, LoadCallbackInterface callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();

        // Tên file người dùng nhập hoặc từ hệ thống
        String finalFileName = (customFileName != null && !customFileName.trim().isEmpty())
                ? customFileName.trim()
                : getFileName(context, fileUri);

        MediaManager.get().upload(fileUri)
                .unsigned("HealthCheckerApp")
                .option("folder", "health_images/" + userId)
                .option("resource_type", "auto")
                .option("public_id", finalFileName)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d("Cloudinary", "Bắt đầu upload: " + finalFileName);
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        // Optional: handle progress UI
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        String format = (String) resultData.get("format");

                        Map<String, Object> fileData = new HashMap<>();
                        fileData.put("url", url);
                        fileData.put("type", format);
                        fileData.put("timestamp", FieldValue.serverTimestamp());
                        fileData.put("name", finalFileName);
                        fileData.put("uploadedBy", userId);

                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(userId)
                                .collection("uploads")
                                .add(fileData)
                                .addOnSuccessListener(docRef -> {
                                    Log.d("Cloudinary", "Thông tin file đã lưu Firestore");

                                    FileItem item = new FileItem(
                                            url,
                                            finalFileName,
                                            Timestamp.now() // dùng thời gian hiện tại, do serverTimestamp bất đồng bộ
                                    );
                                    List<FileItem> items = new ArrayList<>(List.of(item));

                                    if (callback != null) {
                                        callback.onUploadSuccess(items);
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Log.e("Cloudinary", "Lỗi lưu vào Firestore", e));
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e("Cloudinary", "Upload lỗi: " + error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.w("Cloudinary", "Upload bị hoãn lại: " + error.getDescription());
                    }
                })
                .dispatch(context);
    }

    public void loadImagesFromFirestore(LoadCallbackInterface callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("uploads")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<FileItem> items = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        String url = doc.getString("url");
                        String name = doc.getString("name");
                        Timestamp timestamp = doc.getTimestamp("timestamp");
                        if (url != null && timestamp != null) {
                            items.add(new FileItem(url, name != null ? name : "Unknown", timestamp));
                        }
                    }
                    if (callback != null) callback.onUploadSuccess(items);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onLoadFailure(e);
                });
    }

    // Utility để lấy tên file thật từ Uri (cả PDF và ảnh)
    private String getFileName(Context context, Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver()
                    .query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception e) {
                Log.e("Cloudinary", "Failed to get file name", e);
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }
}
