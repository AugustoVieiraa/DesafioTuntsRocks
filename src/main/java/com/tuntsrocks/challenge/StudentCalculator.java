package com.tuntsrocks.challenge;

public class StudentCalculator {
    
    //calculates and return the average of three int numbers
    public int getAverage(int x, int y, int z){
        int avg = x + y + z;
        avg /= 3;
        return avg;
    }

    //calcultes and return the grade needed
    //the student have: 50 >= average < 70
    //he needs a grade like: (avegageGrade + gradeNeeded)/2 = 50
    public int getGradeNeeded(int averageGrade){
        int finalGrade = 0;
        int gradeNeeded = 0;
        while(finalGrade < 50){
            finalGrade = (averageGrade + gradeNeeded)/2;
            gradeNeeded++;
        }
        gradeNeeded--;//decrements the excess after the while
        return gradeNeeded;
    }

    //calculates and return the frequecy in percentage
    public int getFrequency(int misses, int totalClasses){
        int frequency = 100 - ((misses * 100) / totalClasses);
        return frequency;
    }
}
