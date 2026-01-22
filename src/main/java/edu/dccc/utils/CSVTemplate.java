package edu.dccc.utils;

public interface CSVTemplate {
    String toCSV();               // Logic for saving
    void fromCSV(String[] parts); // Logic for loading
}