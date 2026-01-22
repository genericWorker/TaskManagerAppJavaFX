package edu.dccc.utils;

import java.io.*;
import java.util.Collection;

public class CSVReaderWriter<T extends CSVTemplate> {

    private Collection<T> storage;
    private String filePath;
    private Class<T> type; // This helps us create new objects

    public CSVReaderWriter(String filePath, Collection<T> storage, Class<T> type) {
        this.filePath = filePath;
        this.storage = storage;
        this.type = type;
    }

    public void loadFromCSV() {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // 1. Create a brand new empty object (Task or Contact)
                T item = type.getDeclaredConstructor().newInstance();
                // 2. Tell that object to fill itself with data from the line
                item.fromCSV(line.split(","));
                storage.add(item);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void saveToCSV() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            for (T item : storage) {
                pw.println(item.toCSV()); // Just call the object's own method
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}