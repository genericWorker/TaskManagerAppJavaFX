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

   //  Empty constructor required for CSV Reflection
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

    /**
     * TODO: LAB TASK 1 - COMPARISON LOGIC (THE HEAP BRAIN)
     * This determines the order in the PriorityQueue (Min-Heap).
     * 1. Check Status: COMPLETED tasks always return 1 (sink to bottom).
     * 2. Check Priority: Use natural Enum ordering (URGENT < LOW).
     * 3. Check Due Date: Earlier dates rank higher.
     */
    @Override
    public int compareTo(Task other) {
        // TODO: implement priority-based sorting here.
        /* 1. Primary Sort: Use the Priority enum (URGENT should come before LOW),  but only if not COMPLETED
              return this.priority.compareTo(other.getPriority());
         */
        // Logic: COMPLETED tasks usually sink to the bottom regardless of priority.
        if (this.status == TaskStatus.COMPLETED && other.status != TaskStatus.COMPLETED) return 1;
        if (this.status != TaskStatus.COMPLETED && other.status == TaskStatus.COMPLETED) return -1;
        /* 2. Secondary Sort: If priorities are equal, compare by dueDate.
         * 3. Edge Cases: Handle null due dates (e.g., tasks without dates go to the bottom).
         */
        int priorityComparison = this.priority.compareTo(other.getPriority());
        //  This is secondary sort when priorities are equal


        return priorityComparison;
    }

    /**
     * TODO: LAB TASK 3 - Persistence
     * Convert this object into a 7-column CSV string.
     * Handle null dates by returning the string "NULL".
     */
    @Override
    public String toCSV() {
        String startStr = (this.startDate == null) ? "NULL" : this.startDate.toString();
        String dueStr = (this.dueDate == null) ? "NULL" : this.dueDate.toString();
        String compStr = (this.completedDate == null) ? "NULL" : this.completedDate.toString();

  //     return taskId + "," + subject + "," + priority + "," + status + "," +
  //              startStr + "," + dueStr + "," + compStr;
         return "";   // Replace with line above.
    }


    /**
     * TODO: LAB TASK 4 - DESERIALIZATION
     * Reconstruct the object from a String array.
     * Ensure index 6 (completedDate) is handled safely.
     */
    @Override
    public void fromCSV(String[] p) {
       /* try {
            this.taskId = Integer.parseInt(p[0]);
            this.subject = p[1];
            this.priority = Priority.valueOf(p[2]);
            this.status = TaskStatus.valueOf(p[3]);
            this.startDate = (p[4].equalsIgnoreCase("NULL")) ? null : LocalDate.parse(p[4]);
            this.dueDate = (p[5].equalsIgnoreCase("NULL")) ? null : LocalDate.parse(p[5]);
            this.completedDate = (p[6].equalsIgnoreCase("NULL")) ? null : LocalDate.parse(p[6]);
        } catch (Exception e) {
            System.err.println("Error parsing task line: " + String.join(",", p));
        }*/
    }

    // --- Getters and Setters ---
    // Note: In setStatus should logic-check if status == COMPLETED
    // then set completedDate = LocalDate.now()
    // --- Getters and Setters (Required for TableView PropertyValueFactory) ---
    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public Priority getPriority() { return priority; }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public TaskStatus getStatus() { return status; }

    /**
     * TODO: LAB TASK 2 - ENCAPSULATION & LOGIC
     * Update the status. If the status is COMPLETED, automatically set
     * completedDate to today. If it is changed back to ACTIVE, set it to null.
     */
    public void setStatus(TaskStatus status) {
        this.status = status;
        /* TODO: LAB TASK 2 - CONDITIONAL STATE
         * If status is COMPLETED, set completedDate to LocalDate.now().
         * If status is not COMPLETED, set completedDate to null.
         */
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