package uk.ac.manchester.cs.patelt9.twitter;

import com.google.gson.JsonObject;

public interface StreamListener {
    void onJsonReadComplete(JsonObject jo);
} // StreamListener
