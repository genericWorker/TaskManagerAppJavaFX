package edu.dccc.taskmanagerapp;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.*;
import java.time.LocalDate;
import java.util.PriorityQueue;

import javafx.collections.transformation.SortedList;

public class TaskManagerController {

    // FXML UI Components
    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, Integer> colTaskId;
    @FXML private TableColumn<Task, String> colSubject;
    @FXML private TableColumn<Task, Task.Priority> colPriority;
    @FXML private TableColumn<Task, Task.TaskStatus> colStatus;
    @FXML private TableColumn<Task, LocalDate> colStartDate;
    @FXML private TableColumn<Task, LocalDate> colDueDate;

    @FXML private TextField txtSubject;
    @FXML private ComboBox<Task.Priority> comboPriority;
    @FXML private ComboBox<Task.TaskStatus> comboStatus;
    @FXML private DatePicker dpStart;
    @FXML private DatePicker dpDue;

    @FXML private TextField txtSearch;
    @FXML private CheckBox chkHideCompleted;
    @FXML private ProgressBar progressTasks;
    @FXML private Label lblStats;
    @FXML private Label lblUrgentCount;
    @FXML private Label lblSystemMessage;
    @FXML private Label lblClock;
    @FXML private Button btnSubmit;

    private ObservableList<Task> taskList = FXCollections.observableArrayList();
    // 1. Define your Data Store
    private PriorityQueue<Task> taskQueue = new PriorityQueue<>();

    private FilteredList<Task> filteredData;
    private final String CSV_FILE = "tasks.csv";

    public void initialize() {
        // 1. Setup Table Columns
        colTaskId.setCellValueFactory(new PropertyValueFactory<>("taskId"));
        colSubject.setCellValueFactory(new PropertyValueFactory<>("subject"));
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colDueDate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));

        // Set custom comparator
        colPriority.setComparator((p1, p2) -> {
            if (p1 == p2) return 0;
            if (p1 == null) return 1;
            if (p2 == null) return -1;
            return p1.compareTo(p2);
        });
        colPriority.setSortType(TableColumn.SortType.ASCENDING);


        // 2. CONSOLIDATED SELECTION LISTENER
        taskTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            // Only auto-populate if the table itself has focus.
            // This prevents the "search auto-fill" while typing.
            if (newVal != null && taskTable.isFocused()) {
                populateForm(newVal);
            } else if (newVal == null) {
                btnSubmit.setText("SAVE TASK");
                btnSubmit.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
            }
        });

        // 2b. MOUSE CLICK OVERRIDE (Add this right below the listener)
        taskTable.setOnMouseClicked(event -> {
            Task selected = taskTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                // Force focus to the table so the listener above recognizes the interaction
                taskTable.requestFocus();
                populateForm(selected);
            }
        });

        // 3. APPLY GHOST-PROOF FACTORIES
        setupCellFactories();

        // 4. Setup Data Pipeline
        filteredData = new FilteredList<>(taskList, p -> true);
        SortedList<Task> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(taskTable.comparatorProperty());
        taskTable.setItems(sortedData);

        // 5. Search Listener (With Selection Clear)
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            // Force clear selection whenever typing happens
            taskTable.getSelectionModel().clearSelection();

            filteredData.setPredicate(task -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return task.getSubject().toLowerCase().contains(lowerCaseFilter);
            });
        });

        // 6. Enums & Styling
        comboPriority.setItems(FXCollections.observableArrayList(Task.Priority.values()));
        comboStatus.setItems(FXCollections.observableArrayList(Task.TaskStatus.values()));
        setupEnumFormatters();
        taskTable.setStyle("-fx-font-size: 12px;");
        lblClock.setText(LocalDate.now().toString());

        loadTasksFromCSV();
        updateStatistics();
    }

    private void populateForm(Task task) {
        txtSubject.setText(task.getSubject());
        comboPriority.setValue(task.getPriority());
        comboStatus.setValue(task.getStatus());
        dpStart.setValue(task.getStartDate());
        dpDue.setValue(task.getDueDate());

        btnSubmit.setText("UPDATE TASK");
        btnSubmit.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold;");
    }

    private void setupCellFactories() {
        // 1. Priority Column (Ghost-Proof)
        colPriority.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Task.Priority item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    switch (item) {
                        case URGENT -> setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold; -fx-background-color: #fdf2f2;");
                        case HIGH -> setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold;");
                        case NORMAL -> setStyle("-fx-text-fill: #2c3e50;");
                        case LOW -> setStyle("-fx-text-fill: #bdc3c7;");
                    }
                }
            }
        });

        // 2. Start Date Column (Ghost-Proof)
        colStartDate.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else if (item == null) {
                    setText("TBD");
                    setStyle("-fx-text-fill: #bdc3c7; -fx-font-style: italic; -fx-font-size: 11px;");
                } else {
                    setText(item.toString());
                    setStyle("-fx-text-fill: #2c3e50; -fx-font-style: normal; -fx-font-size: 11px;");
                }
            }
        });

        // 3. Due Date Column (Ghost-Proof)
        colDueDate.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else if (item == null) {
                    setText("TBD");
                    setStyle("-fx-text-fill: #bdc3c7; -fx-font-style: italic; -fx-font-size: 11px;");
                } else {
                    setText(item.toString());
                    setStyle("-fx-text-fill: #2c3e50; -fx-font-style: normal; -fx-font-size: 11px;");
                }
            }
        });

        // 4. Status Column (Ghost-Proof)
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Task.TaskStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString().replace("_", " "));
                    if (item == Task.TaskStatus.COMPLETED) setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    else if (item == Task.TaskStatus.IN_PROGRESS) setStyle("-fx-text-fill: #2980b9;");
                    else setStyle("-fx-text-fill: #7f8c8d;");
                }
            }
        });
    }

    private void setupEnumFormatters() {
        StringConverter<Task.Priority> priorityConverter = new StringConverter<>() {
            @Override
            public String toString(Task.Priority p) {
                // Simply return the Enum name (URGENT, HIGH, etc.)
                return (p == null) ? "" : p.name();
            }
            @Override
            public Task.Priority fromString(String s) {
                return null;
            }
        };
        comboPriority.setConverter(priorityConverter);
    }

    private void refreshTableFromQueue() {
        // 1. Sync the master list
        // This maintains the "Semi-Random" heap order from the PriorityQueue
        taskList.clear();
        taskList.addAll(taskQueue);

        // 2. DO NOT change setItems.
        // The table is already looking at sortedData, which is watching filteredData,
        // which is watching taskList. The update propagates automatically.

        // 3. Force the table to refresh its current visual state
        // If a user has clicked a column (like Priority), this reapplies that sort.
        // If no column is clicked, it stays in the "Semi-Random" heap order.
        taskTable.sort();

        // 4. Update the visual stats
        updateStatistics();
    }

    @FXML
    private void handleSave() {
        String subject = txtSubject.getText();
        if (subject == null || subject.isBlank()) {
            updateSystemMessage("Error: Subject is required", "#e74c3c");
            return;
        }

        Task selected = taskTable.getSelectionModel().getSelectedItem();

        if (selected != null) {
            // --- UPDATE MODE ---
            // 1. Remove from Queue to maintain heap integrity
            taskQueue.remove(selected);

            // 2. Modify the object
            selected.setSubject(subject);
            selected.setPriority(comboPriority.getValue());
            selected.setStatus(comboStatus.getValue());
            selected.setStartDate(dpStart.getValue());
            selected.setDueDate(dpDue.getValue());

            // 3. Re-add so the PriorityQueue re-sorts it internally
            taskQueue.add(selected);
            updateSystemMessage("Task Updated", "#2980b9");
        } else {
            // --- ADD MODE ---
            int newId = taskQueue.stream().mapToInt(Task::getTaskId).max().orElse(0) + 1;
            Task newTask = new Task(newId, subject, comboPriority.getValue(),
                    comboStatus.getValue(), dpStart.getValue(), dpDue.getValue());

            taskQueue.add(newTask);
            updateSystemMessage("Task Added", "#27ae60");
        }

        // --- SYNC & PERSIST ---
        saveTasksToCSV();           // Saves from taskQueue
        refreshTableFromQueue();    // Updates taskList from taskQueue (Semi-random order)
        updateStatistics();
        handleClearForm();
    }

    private Task parseTaskFromCSV(String line) {
        String[] data = line.split(",");
        if (data.length < 6) return null;

        try {
            int id = Integer.parseInt(data[0].trim());
            String subject = data[1].trim();
            Task.Priority priority = Task.Priority.valueOf(data[2].trim().toUpperCase());
            Task.TaskStatus status = Task.TaskStatus.valueOf(data[3].trim().toUpperCase());

            // Handle "TBD" (NULL) logic
            String startVal = data[4].trim();
            String dueVal = data[5].trim();
            LocalDate start = startVal.equalsIgnoreCase("NULL") ? null : LocalDate.parse(startVal);
            LocalDate due = dueVal.equalsIgnoreCase("NULL") ? null : LocalDate.parse(dueVal);

            return new Task(id, subject, priority, status, start, due);
        } catch (Exception e) {
            System.out.println("Skipping malformed line: " + line);
            return null;
        }
    }

    @FXML
    private void handleDeleteTask() {
        // 1. Get the item currently highlighted in the table
        Task selected = taskTable.getSelectionModel().getSelectedItem();

        if (selected != null) {
            // 2. Remove from the MASTER Data Store (The Queue)
            // This is the source of truth for your CSV and your table
            taskQueue.remove(selected);

            // 3. Persist the change to the file
            // Ensure your saveTasksToCSV() is now saving from taskQueue!
            saveTasksToCSV();

            // 4. Sync the UI
            // This clears taskList and refills it from the now-shorter taskQueue
            refreshTableFromQueue();

            // 5. Provide Feedback
            updateSystemMessage("DELETED TASK: " + selected.getSubject(), "#e74c3c");
            handleClearForm();
        } else {
            updateSystemMessage("SELECT A TASK TO DELETE", "#f39c12");
        }
    }


    private void loadTasksFromCSV() {
        File file = new File(CSV_FILE);
        if (!file.exists()) return;

        // Clear the Data Store to prevent duplicates
        taskQueue.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // Skip the CSV Header line
            String line;
            while ((line = br.readLine()) != null) {
                Task task = parseTaskFromCSV(line);
                if (task != null) {
                    taskQueue.add(task); // Add to our PriorityQueue
                }
            }
        } catch (IOException e) {
            updateSystemMessage("LOAD ERROR", "#e74c3c");
        }

        // Push the loaded Queue data into the TableView
        refreshTableFromQueue();
    }

    private void refreshTableViewFromQueue() {
        taskList.clear();
        // Iterating a PriorityQueue follows its internal heap order (semi-random)
        taskList.addAll(taskQueue);
        taskTable.setItems(taskList);
    }

    private void saveTasksToCSV() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(CSV_FILE))) {
            pw.println("ID,Subject,Priority,Status,StartDate,DueDate");
            for (Task t : taskQueue) {
                // Check if dates are null before converting to string
                String startStr = (t.getStartDate() == null) ? "NULL" : t.getStartDate().toString();
                String dueStr = (t.getDueDate() == null) ? "NULL" : t.getDueDate().toString();

                pw.printf("%d,%s,%s,%s,%s,%s%n",
                        t.getTaskId(),
                        t.getSubject(),
                        t.getPriority(),
                        t.getStatus(),
                        startStr,
                        dueStr);
            }
        } catch (IOException e) {
            updateSystemMessage("Error saving data", "#e74c3c");
        }
    }

    @FXML
    private void refreshTable() {
        String searchText = txtSearch.getText().toLowerCase();
        boolean hideDone = chkHideCompleted.isSelected();

        filteredData.setPredicate(task -> {
            boolean matchesSearch = task.getSubject().toLowerCase().contains(searchText);
            boolean matchesVisibility = !hideDone || task.getStatus() != Task.TaskStatus.COMPLETED;
            return matchesSearch && matchesVisibility;
        });
        updateStatistics();
    }

    private void updateStatistics() {
        if (taskList.isEmpty()) {
            lblStats.setText("0/0 Done (0%)");
            progressTasks.setProgress(0);
            lblUrgentCount.setText("Urgent: 0");
            return;
        }

        long completed = taskList.stream().filter(t -> t.getStatus() == Task.TaskStatus.COMPLETED).count();
        long urgent = taskList.stream().filter(t -> t.getPriority() == Task.Priority.URGENT).count();

        double percent = (double) completed / taskList.size();
        lblStats.setText(String.format("%d/%d Done (%.0f%%)", completed, taskList.size(), percent * 100));
        progressTasks.setProgress(percent);
        lblUrgentCount.setText("Urgent: " + urgent);
    }



    // 2. Update this method to hard-code the 10px size
    private void updateSystemMessage(String message, String color) {
        lblSystemMessage.setText(message.toUpperCase());
        // Locking font-size to 10px here
        lblSystemMessage.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 10px;");

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> {
            lblSystemMessage.setText("SYSTEM READY");
            // Ensure reset also uses 10px
            lblSystemMessage.setStyle("-fx-text-fill: #95a5a6; -fx-font-weight: bold; -fx-font-size: 10px;");
        });
        pause.play();
    }

    @FXML
    private void handleClearForm() {
        // 1. Clear search (The FilteredList will now show everything)
        if (txtSearch != null) {
            txtSearch.clear();
        }

        // 2. Clear Input Form
        txtSubject.clear();
        comboPriority.setValue(Task.Priority.NORMAL);
        comboStatus.setValue(Task.TaskStatus.NOT_STARTED);
        dpStart.setValue(null);
        dpDue.setValue(null);

        // 3. Reset Button and Selection
        taskTable.getSelectionModel().clearSelection();
        btnSubmit.setText("SAVE TASK");
        btnSubmit.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");

        // 4. RESTORE SEMI-RANDOM ORDER
        // Remove the sort arrows. This tells the SortedList to stop
        // using the Comparator and show the list as it is in the Queue.
        taskTable.getSortOrder().clear();

        // Sync the View with the PriorityQueue (The Logic Store)
        refreshTableFromQueue();

        // 5. Update Status (10px font)
        updateSystemMessage("FORM RESET TO QUEUE ORDER", "#8e44ad");
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }
}