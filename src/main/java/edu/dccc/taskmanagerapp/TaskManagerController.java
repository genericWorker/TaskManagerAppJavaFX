package edu.dccc.taskmanagerapp;

import edu.dccc.utils.CSVReaderWriter;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.PriorityQueue;

public class TaskManagerController {

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
    private PriorityQueue<Task> taskQueue = new PriorityQueue<>();
    private FilteredList<Task> filteredData;
    private CSVReaderWriter<Task> csvService;
    private final String CSV_FILE = "tasks.csv";

    public void initialize() {
        // 1. Setup Table Columns & Custom Sorting
        colTaskId.setCellValueFactory(new PropertyValueFactory<>("taskId"));
        colSubject.setCellValueFactory(new PropertyValueFactory<>("subject"));
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colDueDate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));

        colPriority.setComparator((p1, p2) -> {
            if (p1 == p2) return 0;
            if (p1 == null) return 1;
            if (p2 == null) return -1;
            return p1.compareTo(p2);
        });
        colPriority.setSortType(TableColumn.SortType.ASCENDING);

        // 2. Setup Selection Listeners
        setupSelectionListeners();

        // 3. Apply Visual Styles (Clean Method References)
        setupCellFactories();

        chkHideCompleted.selectedProperty().addListener((obs, oldVal, newVal) -> refreshTable());
        csvService = new CSVReaderWriter<>(CSV_FILE, taskQueue, Task.class);

        // 4. Setup Data Pipeline
        filteredData = new FilteredList<>(taskList, p -> true);
        SortedList<Task> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(taskTable.comparatorProperty());
        taskTable.setItems(sortedData);

        loadTasks();

        // 5. Search Logic
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            taskTable.getSelectionModel().clearSelection();
            filteredData.setPredicate(task -> {
                if (newValue == null || newValue.isEmpty()) return true;
                return task.getSubject().toLowerCase().contains(newValue.toLowerCase());
            });
        });

        // 6. Enums & Final touches
        comboPriority.setItems(FXCollections.observableArrayList(Task.Priority.values()));
        comboStatus.setItems(FXCollections.observableArrayList(Task.TaskStatus.values()));
        setupEnumFormatters();
        taskTable.setStyle("-fx-font-size: 12px;");
        lblClock.setText(LocalDate.now().toString());
    }

    // --- INITIALIZATION HELPERS ---

    private void setupSelectionListeners() {
        taskTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && taskTable.isFocused()) {
                populateForm(newVal);
            } else if (newVal == null) {
                btnSubmit.setText("SAVE TASK");
                btnSubmit.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
            }
        });

        taskTable.setOnMouseClicked(event -> {
            Task selected = taskTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                taskTable.requestFocus();
                populateForm(selected);
            }
        });
    }

    private void setupCellFactories() {
        colPriority.setCellFactory(this::createPriorityCell);
        colStatus.setCellFactory(this::createStatusCell);
        colStartDate.setCellFactory(this::createDateCell);
        colDueDate.setCellFactory(this::createDateCell);
    }

    // --- REUSABLE FACTORY METHODS (GHOST-PROOF) ---

    private TableCell<Task, Task.Priority> createPriorityCell(TableColumn<Task, Task.Priority> col) {
        return new TableCell<>() {
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
        };
    }

    private TableCell<Task, LocalDate> createDateCell(TableColumn<Task, LocalDate> col) {
        return new TableCell<>() {
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
        };
    }

    private TableCell<Task, Task.TaskStatus> createStatusCell(TableColumn<Task, Task.TaskStatus> col) {
        return new TableCell<>() {
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
        };
    }

    // --- CORE LOGIC METHODS ---

    private void loadTasks() {
        taskQueue.clear();
        csvService.loadFromCSV(true);
        refreshTableFromQueue();
    }

    private void saveTasksToCSV() {
        String header = "ID,Subject,Priority,Status,StartDate,DueDate";
        csvService.saveToCSVSorted(header);
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

    private void setupEnumFormatters() {
        comboPriority.setConverter(new StringConverter<>() {
            @Override public String toString(Task.Priority p) { return (p == null) ? "" : p.name(); }
            @Override public Task.Priority fromString(String s) { return null; }
        });
    }

    private void refreshTableFromQueue() {
        taskList.clear();
        taskList.addAll(taskQueue);
        taskTable.sort();
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
        String successMessage; // Create a variable to hold the message
        String color;

        if (selected != null) {
            taskQueue.remove(selected);
            selected.setSubject(subject);
            selected.setPriority(comboPriority.getValue());
            selected.setStatus(comboStatus.getValue());
            selected.setStartDate(dpStart.getValue());
            selected.setDueDate(dpDue.getValue());
            taskQueue.add(selected);
            successMessage = "TASK UPDATED";
            color = "#2980b9";
        } else {
            int newId = taskQueue.stream().mapToInt(Task::getTaskId).max().orElse(0) + 1;
            taskQueue.add(new Task(newId, subject, comboPriority.getValue(), comboStatus.getValue(), dpStart.getValue(), dpDue.getValue()));
            successMessage = "TASK UPDATED";
            color = "#2980b9";

        }
        saveTasksToCSV();
        refreshTableFromQueue();
        handleClearForm();
        updateSystemMessage(successMessage, color);
    }

    @FXML
    private void handleDeleteTask() {
        Task selected = taskTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            taskQueue.remove(selected);
            saveTasksToCSV();
            refreshTableFromQueue();
            updateSystemMessage("Deleted: " + selected.getSubject(), "#e74c3c");
            handleClearForm();
        } else {
            updateSystemMessage("Select a task to delete", "#f39c12");
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

    private void updateSystemMessage(String message, String color) {
        lblSystemMessage.setText(message.toUpperCase());
        lblSystemMessage.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 10px;");
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> {
            lblSystemMessage.setText("SYSTEM READY");
            lblSystemMessage.setStyle("-fx-text-fill: #95a5a6; -fx-font-weight: bold; -fx-font-size: 10px;");
        });
        pause.play();
    }

    @FXML
    private void handleClearForm() {
        executeClear("VIEW RESET: SHOWING TOP PRIORITY", "#8e44ad");
    }

    // Create this helper to do the heavy lifting
    private void executeClear(String message, String color) {
        if (txtSearch != null) txtSearch.clear();
        txtSubject.clear();
        comboPriority.setValue(Task.Priority.NORMAL);
        comboStatus.setValue(Task.TaskStatus.NOT_STARTED);
        dpStart.setValue(null);
        dpDue.setValue(null);

        taskTable.getSelectionModel().clearSelection();
        btnSubmit.setText("SAVE TASK");
        btnSubmit.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");

        taskTable.getSortOrder().clear();
        refreshTableFromQueue();

        // Only update the message if one was provided
        if (message != null) {
            updateSystemMessage(message, color);
        }
    }

    @FXML
    private void handleExit() { System.exit(0); }
}