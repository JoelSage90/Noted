package com.qm;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import org.json.JSONObject;

public class Main {
    public static void main(String[] args) {
        String model = "deepseek-r1:1.5b";
        String prompt = "what is deepseek?";
        String fullResponse = "";

        try{
            //Set up an HTTP POST request
            URL url = new URI("http://localhost:11434/api/generate").toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            // Create request JSON
            JSONObject requestJson = new JSONObject();
            requestJson.put("model", model);
            requestJson.put("prompt", prompt);
            requestJson.put("stream", false); //gives result when done (instead of word by word)
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}