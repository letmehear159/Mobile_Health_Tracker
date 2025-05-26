package com.example.healthtrackerapp.viewmodel;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.ViewModel;

import com.example.healthtrackerapp.repository.CloudinaryRepository;

public class ImageViewModel extends ViewModel {
    private final CloudinaryRepository repository = new CloudinaryRepository();

    public void uploadImageToCloudinary(Context context, Uri imageUri) {
        repository.uploadFile(context, imageUri);
    }
}
