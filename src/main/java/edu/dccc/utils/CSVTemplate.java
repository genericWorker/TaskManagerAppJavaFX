package edu.dccc.utils;

public interface CSVTemplate {
    String toCSV();               // Logic for saving // Object -> String
    void fromCSV(String[] parts); // Logic for loading  // String -> object
}