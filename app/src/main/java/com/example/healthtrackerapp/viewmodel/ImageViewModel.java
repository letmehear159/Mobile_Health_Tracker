package com.example.healthtrackerapp.viewmodel;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.healthtrackerapp.model.FileItem;
import com.example.healthtrackerapp.repository.CloudinaryRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ImageViewModel extends ViewModel {
    private final CloudinaryRepository repository = new CloudinaryRepository();

    MutableLiveData<List<FileItem>> fileItems = new MutableLiveData<>();

    public LiveData<List<FileItem>> getFileItems() {
        return fileItems;
    }

    public void loadImagesFromFirestore() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
                        String name = doc.getString("name"); // có thể null
                        Timestamp timestamp = doc.getTimestamp("timestamp");
                        if (url != null && timestamp != null) {
                            items.add(new FileItem(url, name != null ? name : "Unknown", timestamp));
                        }
                    }
                    fileItems.setValue(items);
                });
    }

    public void uploadFileToCloudinary(Context context, Uri imageUri, String fileName) {
        repository.uploadFile(context, imageUri, fileName, uploadedItem -> {
            List<FileItem> current = fileItems.getValue();
            if (current == null) current = new ArrayList<>();
            current.add(0, uploadedItem); // Thêm file mới vào đầu danh sách
            fileItems.postValue(current);
        });
    }

}
