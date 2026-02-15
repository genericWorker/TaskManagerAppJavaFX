package edu.dccc.taskmanagerapp;

import edu.dccc.utils.CSVTemplate;
import java.time.LocalDate;

public class Task implements Comparable<Task>, CSVTemplate {

    public enum Priority { URGENT, HIGH, NORMAL, LOW }
    public enum TaskStatus { NOT_STARTED, IN_PROGRESS, COMPLETED }

    private int taskId;
    private String subject;
    private Priority priority;
    private TaskStatus status;
    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDate completedDate; // New Field

    public Task() {
        this.priority = Priority.NORMAL;
        this.status = TaskStatus.NOT_STARTED;
    }

  public Task(int taskId, String subject, Priority priority, TaskStatus status,
                LocalDate startDate, LocalDate dueDate, LocalDate completedDate) {
        this.taskId = taskId;
        this.subject = subject;
        this.priority = (priority == null) ? Priority.NORMAL : priority;
        this.status = (status == null) ? TaskStatus.NOT_STARTED : status;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.completedDate = completedDate;
    }

    public Task(int taskId, String subject, Priority priority, LocalDate start, LocalDate due) {
        // New tasks always start as NOT_STARTED and have no completedDate yet.
        this(taskId, subject, priority, TaskStatus.NOT_STARTED, start, due, null);
    }

    @Override
    public int compareTo(Task other) {
        // TODO: Students implement priority-based sorting here.
        // Logic: COMPLETED tasks usually sink to the bottom regardless of priority.
        if (this.status == TaskStatus.COMPLETED && other.status != TaskStatus.COMPLETED) return 1;
        if (this.status != TaskStatus.COMPLETED && other.status == TaskStatus.COMPLETED) return -1;

        int priorityComparison = this.priority.compareTo(other.getPriority());
        if (priorityComparison == 0) {
            if (this.dueDate == null) return 1;
            if (other.dueDate == null) return -1;
            return this.dueDate.compareTo(other.dueDate);
        }
        return priorityComparison;
    }

    @Override
    public String toCSV() {
        String startStr = (this.startDate == null) ? "NULL" : this.startDate.toString();
        String dueStr = (this.dueDate == null) ? "NULL" : this.dueDate.toString();
        String compStr = (this.completedDate == null) ? "NULL" : this.completedDate.toString();

        return taskId + "," + subject + "," + priority + "," + status + "," +
                startStr + "," + dueStr + "," + compStr;
    }

    @Override
    public void fromCSV(String[] p) {
        try {
            this.taskId = Integer.parseInt(p[0]);
            this.subject = p[1];
            this.priority = Priority.valueOf(p[2]);
            this.status = TaskStatus.valueOf(p[3]);
            this.startDate = (p[4].equalsIgnoreCase("NULL")) ? null : LocalDate.parse(p[4]);
            this.dueDate = (p[5].equalsIgnoreCase("NULL")) ? null : LocalDate.parse(p[5]);
            this.completedDate = (p[6].equalsIgnoreCase("NULL")) ? null : LocalDate.parse(p[6]);
        } catch (Exception e) {
            System.err.println("Error parsing task line: " + String.join(",", p));
        }
    }

    // --- Getters and Setters ---
    // Note: In setStatus, students should logic-check if status == COMPLETED
    // then set completedDate = LocalDate.now()
    // --- Getters and Setters (Required for TableView PropertyValueFactory) ---
    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public TaskStatus getStatus() { return status; }

    public void setStatus(TaskStatus status) {
        this.status = status;
        // Logic: If moving to COMPLETED, stamp the date.
        // If moving AWAY from COMPLETED, clear the date.
        if (status == TaskStatus.COMPLETED) {
            this.completedDate = LocalDate.now();
        } else {
            this.completedDate = null;
        }
    }

    public LocalDate getCompletedDate() { return completedDate; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public void setCompletedDate(LocalDate completedDate) {
        this.completedDate = completedDate;
    }

    @Override
    public String toString() {
        return "Task #" + taskId + ": " + subject + " [" + priority + "]";
    }
}