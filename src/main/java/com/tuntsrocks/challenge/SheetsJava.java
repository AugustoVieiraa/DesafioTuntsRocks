package com.tuntsrocks.challenge;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

public class SheetsJava {
    private static String APPLICATION_NAME = "Google Sheets API Java for Tunts Rocks Challenge";
    private static String SPREADSHEET_ID = "1C01Qnkt_3ca0_CZhAf8389M2uzlV67QC9Ylvcilzxnc";
    private static Sheets sheetsService;
    
    private static Credential authorize() throws IOException, GeneralSecurityException{
        InputStream in = SheetsJava.class.getResourceAsStream("/credentials.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
            GsonFactory.getDefaultInstance(), new InputStreamReader(in));

        List<String> scopes = Arrays.asList(SheetsScopes.SPREADSHEETS);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
            GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), clientSecrets, scopes)
            .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
            .setAccessType("offline")
            .build();
        Credential credential = new AuthorizationCodeInstalledApp(
            flow, new LocalServerReceiver())
            .authorize("user");

        return credential;
    }

    public static Sheets getSheetsService() throws IOException, GeneralSecurityException{
        Credential credential = authorize();
        return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(), credential)
            .setApplicationName(APPLICATION_NAME)
            .build();
    }

    //function to see the log
    public static void getLogSheet(int frequency, int averageGrade, int gradeNeeded){
        //log for reference
        System.out.println("Frequency: "+frequency+"%");
        System.out.println("Average grade: "+averageGrade);
        System.out.println("Grade needed: "+gradeNeeded+"\n");
        //end log
    }

    //function to update de fields in the spreadsheet (fields: 'Situacao' and 'Nota para Aprovacao Final')
    public static void updateSheet(String situationSheet, int gradeSheet, int studentSheet) throws IOException{
        ValueRange body = new ValueRange()
            .setValues(Arrays.asList(
                Arrays.asList(situationSheet, gradeSheet)));
                
        UpdateValuesResponse result = sheetsService.spreadsheets().values()
            .update(SPREADSHEET_ID, "G"+studentSheet, body)
            .setValueInputOption("RAW")
            .execute();
    }

    public static void main(String[] args) throws IOException, GeneralSecurityException{
        sheetsService = getSheetsService();
        int averageGrade = 0;
        int gradeNeeded = 0;
        int studentLine = 4;//line of the first student in the spreadsheet
        int totalClasses = 60;
        int frequency = 0;
        StudentCalculator stdCalc = new StudentCalculator();

        //reading data in the range of the spreadsheet
        String range = "A4:F27";

        ValueRange response = sheetsService.spreadsheets().values()
            .get(SPREADSHEET_ID, range)
            .execute();

        List<List<Object>> values = response.getValues();

        //verification if the list values is null or empty
        if(values == null || values.isEmpty()){
            System.out.println("No data found!!!\n");
        } 
        else{
            for(List row : values){
                //calculates the frequency in class of each student
                //represented by: 100 - ((misses * 100) / totalClasses)

                frequency = stdCalc.getFrequency(Integer.parseInt((String) row.get(2)), totalClasses);                 

                if(frequency < 75){//if the student missed more than 25% of the total classes
                    
                    //puts in the spreadsheet "Reprovado por Falta" in "Situação" field
                    //puts in the spreadsheet "0" in "Nota para Aprovação Final" field
                    //update the spreadsheet
                    updateSheet("Reprovado por Falta", 0, studentLine);

                    //log for reference
                    System.out.println("Added in the spreadsheet 'Reprovado por Falta' and '0' in the line: "+studentLine);
                    getLogSheet(frequency, averageGrade, gradeNeeded);
             
                    studentLine++;//increments the line for the next student in the spreadsheet
                }
                else{//student missed less then 25% of the total of classes
                    
                    //calculate the average grade of the three grades
                    averageGrade = stdCalc.getAverage(Integer.parseInt((String) row.get(3)), Integer.parseInt((String)row.get(4)), Integer.parseInt((String)row.get(5)));

                    if(averageGrade >= 70){
                        //puts in the spreadsheet "Aprovado" in "Situação" field
                        //puts in the spreadsheet "0" in "Nota para Aprovação Final" field
                        //update the spreadsheet
                        updateSheet("Aprovado", 0, studentLine);

                        //log for reference
                        System.out.println("Added in the spreadsheet 'Aprovado' and '0' in the line: "+studentLine);
                        getLogSheet(frequency, averageGrade, gradeNeeded);
  
                        studentLine++;//increments the line for the next student in the spreadsheet
                    }
                    else if(averageGrade < 50){
                        //puts in the spreadsheet "Reprovado por Nota" in "Situação" field
                        //puts in the spreadsheet "0" in "Nota para Aprovação Final" field
                        //update the spreadsheet
                        updateSheet("Reprovado por Nota", 0, studentLine);

                        //log for reference
                        System.out.println("Added in the spreadsheet 'Reprovado por Nota' and '0' in the line: "+studentLine);
                        getLogSheet(frequency, averageGrade, gradeNeeded);
 
                        studentLine++;//increments the line for the next student in the spreadsheet
                    }
                    else{
                        //puts in the spreadsheet "Exame Final" in "Situação" field
                        //calculate the final grade needed (gradeNeeded)
                        //puts in the spreadsheet the value of "gradeNeeded" in "Notas para Aprovação Final" field

                        gradeNeeded = stdCalc.getGradeNeeded(averageGrade);
                        //update the spreadsheet
                        updateSheet("Exame Final", gradeNeeded, studentLine);
                        //log for reference
                        System.out.printf("Added in the spreadsheet 'Exame Final' and '"+gradeNeeded+"' in the line: "+studentLine+"\n");
                        getLogSheet(frequency, averageGrade, gradeNeeded);

                        studentLine++;//increments the line for the next student in the spreadsheet
                        gradeNeeded = 0;
                    }
                }
            }
        }
    }
}