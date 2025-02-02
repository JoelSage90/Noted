package com.qm;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import org.json.JSONObject;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        String model = "deepseek-r1:1.5b";
        String fullResponse = "";

        //takes user input for notes and number of questions
        Scanner s = new Scanner(System.in);
        System.out.println("enter number of questions: ");
        String num = s.nextLine();

        StringBuilder notes = new StringBuilder();
        System.out.println("enter notes (end notes with 'EXIT' for the end): ");
        
        while (true) {
            String line = s.nextLine();
            if (line.equalsIgnoreCase("EXIT")) {
                break;//ends loop at EXIT
            }
            notes.append(line).append("\n");
        }
        
        s.close();
        String prompt = String.format(
    "Given the following notes, generate"+num.toString()+"multiple-choice questions with 4 options for each question. " +
    "Provide the correct answer key at the end in this format:\n\n" +
    "Questions:\n" +
    "\n\n" +
    "Answers:\n" +
    "\n\n" +
    "Notes: "+ notes.toString());
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

            //send request
            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestJson.toString().getBytes());
            }
            // Get response
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                //response is in a json format so this is cleaned and stored in variable
                String responseLine = br.readLine();
                JSONObject responseJson = new JSONObject(responseLine);
                fullResponse = responseJson.getString("response");
            }

            System.out.println(fullResponse);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}