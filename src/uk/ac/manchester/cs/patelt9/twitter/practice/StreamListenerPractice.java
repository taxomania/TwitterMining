package uk.ac.manchester.cs.patelt9.twitter.practice;

import com.google.gson.JsonObject;

public interface StreamListenerPractice {
    void onJsonReadComplete(JsonObject jo);
} // StreamListener
