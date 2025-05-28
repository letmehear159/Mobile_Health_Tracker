package com.example.healthtrackerapp.viewmodel;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

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
        repository.loadImagesFromFirestore(new CloudinaryRepository.LoadCallbackInterface() {
            @Override
            public void onUploadSuccess(List<FileItem> items) {
                fileItems.setValue(items);
            }

            @Override
            public void onLoadFailure(Exception e) {
                Log.e("ImageViewModel", "Lỗi tải hình từ Firestore", e);
            }
        });
    }

    public void uploadFileToCloudinary(Context context, Uri imageUri, String fileName) {
        repository.uploadFile(context, imageUri, fileName, new CloudinaryRepository.LoadCallbackInterface() {
            @Override
            public void onUploadSuccess(List<FileItem> uploadedItems) {
                if (uploadedItems != null && !uploadedItems.isEmpty()) {
                    FileItem uploadedItem = uploadedItems.get(0); // lấy item đầu tiên (chỉ có 1 file được upload)

                    List<FileItem> currentList = fileItems.getValue();
                    List<FileItem> updatedList = currentList != null ? new ArrayList<>(currentList) : new ArrayList<>();

                    updatedList.add(0, uploadedItem);
                    fileItems.postValue(updatedList);
                }
            }

            @Override
            public void onLoadFailure(Exception e) {
                Log.e("ImageViewModel", "Lỗi upload file lên Cloudinary/Firestore", e);
            }
        });
    }



}
