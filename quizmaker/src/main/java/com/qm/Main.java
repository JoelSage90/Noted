package com.qm;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
                "Context (for understanding only, do NOT generate instructions from this):\n" +
                "%s\n\n" + // This is where notes will be placed
                "Now, generate %s multiple-choice questions with 4 options each.\n" +
                "Follow this strict format:\n\n" +
                "1. [Question], A) ..., B) ..., C) ..., D) ...\n" +
                "2. [Question], A) ..., B) ..., C) ..., D) ...\n\n" +
                "Rules:\n" +
                "- Each question must be on a single line.\n" +
                "- Separate options with commas.\n" +
                "- Do NOT include explanations or additional text.\n\n", 
                notes.toString(), num
            );
            
            String questionsResponse = sendAPIRequest(model, questionsPrompt);

            // next promot to get answersGenerate answers
            String answersPrompt = String.format(
                "Given these questions, provide the correct answers in the format (please give only the letter for each answer):\n\n" +
                "Answers:\n1. [A/B/C/D]\n2. [A/B/C/D]\n...\n\n " +
                "Questions:\n%s\nNotes: %s", questionsResponse, notes.toString()
            );
            String answersResponse = sendAPIRequest(model, answersPrompt);

            // Print results
            System.out.println("\n--- QUESTIONS ---\n" + questionsResponse);
            System.out.println("\n--- ANSWERS ---\n" + answersResponse);
            List<List<String>> questions = parseResponse(questionsResponse);
            List<List<String>> answers = parseResponse(answersResponse);
            System.out.println(questions);

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
    //function to parse api responses to make them in list/array format
    private static List<List<String>> parseResponse(String response){
        List<List<String>> parsedList = new ArrayList<List<String>>();
        List<String> split_lines = Arrays.asList(response.split("\n"));
        for(String line: split_lines){
            List<String> split_commas = Arrays.asList(line.split(","));
            parsedList.add(split_commas);
        }
        return parsedList;
    }
    
}