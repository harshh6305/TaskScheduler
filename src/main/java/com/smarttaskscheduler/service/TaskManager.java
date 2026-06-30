package com.smarttaskscheduler.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.smarttaskscheduler.model.Task;
import com.smarttaskscheduler.model.Status;
import com.smarttaskscheduler.util.LocalDateTimeAdapter;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class TaskManager {

    private PriorityQueue<Task> taskQueue;
    private static final String DATA_FILE = "tasks.json";
    private Gson gson;

    public TaskManager() {
        // Comparator: High priority first, then closest deadline
        Comparator<Task> taskComparator = (t1, t2) -> {
            int priorityCompare = Integer.compare(t2.getPriority().getLevel(), t1.getPriority().getLevel());
            if (priorityCompare != 0) {
                return priorityCompare;
            }
            return t1.getDeadline().compareTo(t2.getDeadline());
        };

        this.taskQueue = new PriorityQueue<>(taskComparator);
        
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
                
        loadTasks();
    }

    public void addTask(Task task) {
        taskQueue.offer(task);
        saveTasks();
    }

    public void removeTask(Task task) {
        taskQueue.remove(task);
        saveTasks();
    }

    public void updateTask(Task oldTask, Task newTask) {
        taskQueue.remove(oldTask);
        taskQueue.offer(newTask);
        saveTasks();
    }

    public void completeTask(Task task) {
        taskQueue.remove(task);
        task.setStatus(Status.COMPLETED);
        taskQueue.offer(task);
        saveTasks();
    }

    public List<Task> getAllTasks() {
        List<Task> sortedList = new ArrayList<>();
        PriorityQueue<Task> tempQueue = new PriorityQueue<>(taskQueue.comparator());
        tempQueue.addAll(taskQueue);
        while (!tempQueue.isEmpty()) {
            sortedList.add(tempQueue.poll());
        }
        return sortedList;
    }

    private void loadTasks() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return;
        }
        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<Task>>(){}.getType();
            List<Task> loadedTasks = gson.fromJson(reader, listType);
            if (loadedTasks != null) {
                taskQueue.addAll(loadedTasks);
            }
        } catch (IOException e) {
            System.err.println("Failed to load tasks: " + e.getMessage());
        }
    }

    public void saveTasks() {
        try (Writer writer = new FileWriter(DATA_FILE)) {
            List<Task> tasks = new ArrayList<>(taskQueue);
            gson.toJson(tasks, writer);
        } catch (IOException e) {
            System.err.println("Failed to save tasks: " + e.getMessage());
        }
    }
}
