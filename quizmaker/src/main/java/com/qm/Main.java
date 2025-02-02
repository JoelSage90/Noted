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
        try {
            //first promot to Generate questions
            String questionsPrompt = String.format(
                "Generate %s multiple-choice questions with 4 options each. " +
                "Include ONLY the questions (no answers) in this format:\n\n" +
                "Questions:\n1. [Question]?\nA) ...\nB) ...\nC) ...\nD) ...\n\n" +
                "Notes: %s", num, notes.toString()
            );
            String questionsResponse = sendAPIRequest(model, questionsPrompt);

            // next promot to get answersGenerate answers
            String answersPrompt = String.format(
                "Given these questions, provide the correct answers in the format:\n\n" +
                "Answers:\n1. [Correct Option]\n2. [Correct Option]\n...\n\n" +
                "Questions:\n%s\nNotes: %s", questionsResponse, notes.toString()
            );
            String answersResponse = sendAPIRequest(model, answersPrompt);

            // Print results
            System.out.println("\n--- QUESTIONS ---\n" + questionsResponse);
            System.out.println("\n--- ANSWERS ---\n" + answersResponse);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //method to for api requests
    private static String sendAPIRequest(String model, String prompt) throws Exception {
        URL url = new URI("http://localhost:11434/api/generate").toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        JSONObject requestJson = new JSONObject();
        requestJson.put("model", model);
        requestJson.put("prompt", prompt);
        requestJson.put("stream", false);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestJson.toString().getBytes());
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String responseLine = br.readLine();
            JSONObject responseJson = new JSONObject(responseLine);
            String fullResponse = responseJson.getString("response");
            return fullResponse.replaceAll("(?s)<think>.*?</think>", "").trim();
        }
    }
}