package com.smarttaskscheduler.ui;

import com.smarttaskscheduler.model.Priority;
import com.smarttaskscheduler.model.Status;
import com.smarttaskscheduler.model.Task;
import com.smarttaskscheduler.service.TaskManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class MainWindow {

    private final TaskManager taskManager;
    private final BorderPane view;
    private final ListView<Task> taskListView;
    private final ObservableList<Task> observableTasks;
    private Timer reminderTimer;
    private String currentFilter = "ALL";

    // Form fields
    private TextField titleField;
    private TextArea descArea;
    private ComboBox<Priority> priorityCombo;
    private DatePicker deadlineDatePicker;
    private TextField timeField;
    private Task selectedTask = null;

    public MainWindow() {
        taskManager = new TaskManager();
        view = new BorderPane();
        view.setPadding(new Insets(10));
        
        observableTasks = FXCollections.observableArrayList();
        taskListView = new ListView<>(observableTasks);
        taskListView.setCellFactory(param -> new ListCell<Task>() {
            @Override
            protected void updateItem(Task item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(String.format("[%s] %s - Due: %s (%s)", 
                        item.getPriority(), item.getTitle(), item.getDeadline(), item.getStatus()));
                    if (item.getStatus() == Status.COMPLETED) {
                        setTextFill(Color.GRAY);
                    } else if (item.getDeadline().isBefore(LocalDateTime.now())) {
                        setTextFill(Color.RED);
                    } else {
                        setTextFill(Color.BLACK);
                    }
                }
            }
        });
        
        taskListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                populateForm(newSel);
            }
        });

        view.setTop(createToolbar());
        view.setCenter(taskListView);
        view.setRight(createSidebar());

        refreshList();
        startReminderTimer();
    }

    public BorderPane getView() {
        return view;
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(0, 0, 10, 0));
        
        Button btnAll = new Button("All Tasks");
        Button btnToday = new Button("Today's Tasks");
        Button btnHigh = new Button("High Priority");
        Button btnCompleted = new Button("Completed");

        btnAll.setOnAction(e -> applyFilter("ALL"));
        btnToday.setOnAction(e -> applyFilter("TODAY"));
        btnHigh.setOnAction(e -> applyFilter("HIGH"));
        btnCompleted.setOnAction(e -> applyFilter("COMPLETED"));

        toolbar.getChildren().addAll(btnAll, btnToday, btnHigh, btnCompleted);
        return toolbar;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(0, 0, 0, 10));
        sidebar.setPrefWidth(300);

        Label lblTitle = new Label("Task Details");
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        titleField = new TextField();
        titleField.setPromptText("Title");

        descArea = new TextArea();
        descArea.setPromptText("Description");
        descArea.setPrefRowCount(4);

        priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll(Priority.values());
        priorityCombo.setValue(Priority.MEDIUM);

        deadlineDatePicker = new DatePicker(LocalDate.now());
        timeField = new TextField("23:59");
        timeField.setPromptText("HH:mm");

        Button btnSave = new Button("Add / Update Task");
        btnSave.setMaxWidth(Double.MAX_VALUE);
        btnSave.setOnAction(e -> saveTask());

        Button btnComplete = new Button("Mark Complete");
        btnComplete.setMaxWidth(Double.MAX_VALUE);
        btnComplete.setOnAction(e -> completeSelectedTask());

        Button btnDelete = new Button("Delete Task");
        btnDelete.setMaxWidth(Double.MAX_VALUE);
        btnDelete.setOnAction(e -> deleteSelectedTask());
        
        Button btnClear = new Button("Clear Form");
        btnClear.setMaxWidth(Double.MAX_VALUE);
        btnClear.setOnAction(e -> clearForm());

        sidebar.getChildren().addAll(
            lblTitle, 
            new Label("Title:"), titleField, 
            new Label("Description:"), descArea, 
            new Label("Priority:"), priorityCombo, 
            new Label("Deadline Date:"), deadlineDatePicker, 
            new Label("Deadline Time (HH:mm):"), timeField, 
            new Separator(), btnSave, btnComplete, btnDelete, btnClear
        );

        return sidebar;
    }

    private void populateForm(Task task) {
        this.selectedTask = task;
        titleField.setText(task.getTitle());
        descArea.setText(task.getDescription());
        priorityCombo.setValue(task.getPriority());
        deadlineDatePicker.setValue(task.getDeadline().toLocalDate());
        timeField.setText(task.getDeadline().toLocalTime().toString());
    }

    private void clearForm() {
        this.selectedTask = null;
        titleField.clear();
        descArea.clear();
        priorityCombo.setValue(Priority.MEDIUM);
        deadlineDatePicker.setValue(LocalDate.now());
        timeField.setText("23:59");
        taskListView.getSelectionModel().clearSelection();
    }

    private void saveTask() {
        if (titleField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Title cannot be empty.");
            return;
        }

        LocalTime time;
        try {
            time = LocalTime.parse(timeField.getText().trim());
        } catch (DateTimeParseException ex) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Invalid time format. Use HH:mm");
            return;
        }

        LocalDateTime deadline = LocalDateTime.of(deadlineDatePicker.getValue(), time);
        
        if (selectedTask == null) {
            Task newTask = new Task(
                titleField.getText().trim(),
                descArea.getText().trim(),
                priorityCombo.getValue(),
                deadline
            );
            taskManager.addTask(newTask);
        } else {
            Task updatedTask = new Task(
                titleField.getText().trim(),
                descArea.getText().trim(),
                priorityCombo.getValue(),
                deadline
            );
            updatedTask.setId(selectedTask.getId());
            updatedTask.setStatus(selectedTask.getStatus());
            taskManager.updateTask(selectedTask, updatedTask);
        }

        clearForm();
        refreshList();
    }

    private void completeSelectedTask() {
        if (selectedTask != null) {
            taskManager.completeTask(selectedTask);
            clearForm();
            refreshList();
        }
    }

    private void deleteSelectedTask() {
        if (selectedTask != null) {
            taskManager.removeTask(selectedTask);
            clearForm();
            refreshList();
        }
    }

    private void applyFilter(String filter) {
        this.currentFilter = filter;
        refreshList();
    }

    private void refreshList() {
        List<Task> allTasks = taskManager.getAllTasks();
        List<Task> filtered;

        switch (currentFilter) {
            case "TODAY":
                LocalDate today = LocalDate.now();
                filtered = allTasks.stream()
                        .filter(t -> t.getDeadline().toLocalDate().equals(today))
                        .collect(Collectors.toList());
                break;
            case "HIGH":
                filtered = allTasks.stream()
                        .filter(t -> t.getPriority() == Priority.HIGH)
                        .collect(Collectors.toList());
                break;
            case "COMPLETED":
                filtered = allTasks.stream()
                        .filter(t -> t.getStatus() == Status.COMPLETED)
                        .collect(Collectors.toList());
                break;
            case "ALL":
            default:
                filtered = allTasks;
                break;
        }

        observableTasks.setAll(filtered);
    }

    private void startReminderTimer() {
        reminderTimer = new Timer(true);
        reminderTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkDeadlines();
            }
        }, 0, 60000); // Check every minute
    }

    private void checkDeadlines() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);

        List<Task> dueSoon = taskManager.getAllTasks().stream()
                .filter(t -> t.getStatus() == Status.PENDING)
                .filter(t -> t.getDeadline().isAfter(now) && t.getDeadline().isBefore(oneHourLater))
                .collect(Collectors.toList());

        if (!dueSoon.isEmpty()) {
            Platform.runLater(() -> {
                StringBuilder message = new StringBuilder("The following tasks are due within an hour:\n\n");
                for (Task t : dueSoon) {
                    message.append("- ").append(t.getTitle()).append(" (Due: ").append(t.getDeadline().toLocalTime()).append(")\n");
                }
                showAlert(Alert.AlertType.INFORMATION, "Task Reminder", message.toString());
            });
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void onShutdown() {
        if (reminderTimer != null) {
            reminderTimer.cancel();
        }
        taskManager.saveTasks();
    }
}
