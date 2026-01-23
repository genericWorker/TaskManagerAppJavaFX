package edu.dccc.taskmanagerapp;

import edu.dccc.utils.CSVTemplate;
import java.time.LocalDate;

public class Task implements Comparable<Task>, CSVTemplate {

    // Nested Enums
    public enum Priority { URGENT, HIGH, NORMAL, LOW }
    public enum TaskStatus { NOT_STARTED, IN_PROGRESS, COMPLETED }

    // Fields
    private int taskId;
    private String subject;
    private Priority priority;
    private TaskStatus status;
    private LocalDate startDate;
    private LocalDate dueDate;

    // 1. MANDATORY: Default constructor for CSVReaderWriter
    public Task() {
        // Initialize with defaults so a "blank" task doesn't crash the UI
        this.priority = Priority.NORMAL;
        this.status = TaskStatus.NOT_STARTED;
    }

    // 2. Complete constructor for creating new tasks from the UI
    public Task(int taskId, String subject, Priority priority, TaskStatus status, LocalDate startDate, LocalDate dueDate) {
        this.taskId = taskId;
        this.subject = subject;

        // Safety: If UI sends null, don't break the PriorityQueue logic
        this.priority = (priority == null) ? Priority.NORMAL : priority;
        this.status = (status == null) ? TaskStatus.NOT_STARTED : status;

        this.startDate = startDate;
        this.dueDate = dueDate;
    }

    // 3. CSVTemplate Implementation: How to save to a string
    @Override
    public String toCSV() {
        // Convert dates to "NULL" string if they are null, otherwise use standard ISO format
        String startStr = (this.startDate == null) ? "NULL" : this.startDate.toString();
        String dueStr = (this.dueDate == null) ? "NULL" : this.dueDate.toString();

        return taskId + "," +
                subject + "," +
                priority + "," +
                status + "," +
                startStr + "," +
                dueStr;
    }

    // 4. CSVTemplate Implementation: How to load from parts
    @Override
    public void fromCSV(String[] p) {
        try {
            this.taskId = Integer.parseInt(p[0]);
            this.subject = p[1];
            this.priority = Priority.valueOf(p[2]);
            this.status = TaskStatus.valueOf(p[3]);

            // Handle "NULL" strings during loading
            this.startDate = (p[4].equalsIgnoreCase("NULL")) ? null : LocalDate.parse(p[4]);
            this.dueDate = (p[5].equalsIgnoreCase("NULL")) ? null : LocalDate.parse(p[5]);

        } catch (Exception e) {
            System.err.println("Error parsing task line: " + String.join(",", p));
        }
    }

    // 5. Comparable Implementation: How the PriorityQueue decides who is first
    @Override
    public int compareTo(Task other) {
        // 1. Primary Sort: Priority (URGENT -> HIGH -> NORMAL -> LOW)
        int priorityComparison = this.priority.compareTo(other.getPriority());

        // 2. Secondary Sort: If priorities are equal, sort by Due Date
        if (priorityComparison == 0) {
            // Handle null dates (TBD) safely
            if (this.dueDate == null && other.dueDate == null) return 0;
            if (this.dueDate == null) return 1;  // This task moves down
            if (other.dueDate == null) return -1; // Other task moves down

            return this.dueDate.compareTo(other.dueDate);
        }

        return priorityComparison;
    }



    // --- Getters and Setters (Required for TableView PropertyValueFactory) ---
    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    @Override
    public String toString() {
        return "Task #" + taskId + ": " + subject + " [" + priority + "]";
    }
}