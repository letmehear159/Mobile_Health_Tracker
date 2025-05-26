package com.example.healthtrackerapp.manager;


import android.content.Context;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryManager {
    private static boolean initialized = false;

    public static void init(Context context) {
        if (!initialized) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "drhgtpxyr");
            config.put("api_key", "669518772878855");
            config.put("api_secret", "YY4Eh5SsG4xkhaQ6Xb7SIyPnkKI"); // chỉ test thôi!
            MediaManager.init(context, config);
            initialized = true;
        }
    }
}
