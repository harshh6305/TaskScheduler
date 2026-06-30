# Smart Task Scheduler

A Java desktop application that helps a user manage daily tasks by automatically ordering them based on urgency using a priority queue. Built entirely with core Java and JavaFX.

## Features
- **Task Management**: Add, edit, delete, and mark tasks as complete.
- **Priority Queue**: Tasks are ordered automatically (High Priority first, then closest deadlines).
- **Filtering**: View "All", "Today's Tasks", "High Priority", and "Completed".
- **Reminders**: A background timer checks every minute and alerts you if any pending tasks are due within the next hour.
- **Persistence**: Automatically saves and loads tasks from a local `tasks.json` file on exit and launch.

## Requirements
- Java (JDK 17+)
- Maven (3.6+)

## Building the Project
Run the following command in the project root directory:
```bash
mvn clean package
```
This will compile the project and build an executable "fat" JAR containing all required dependencies (including JavaFX and Gson).

## Running the Application
You can run the application directly using the generated JAR file:
```bash
java -jar target/SmartTaskScheduler-1.0-SNAPSHOT.jar
```
Alternatively, you can run it via the JavaFX Maven Plugin during development:
```bash
mvn javafx:run
```

## Structure
- `com.smarttaskscheduler.model`: Core entities (`Task`, `Priority`, `Status`).
- `com.smarttaskscheduler.service`: Logic and persistence (`TaskManager`).
- `com.smarttaskscheduler.ui`: JavaFX UI views and controllers (`MainApp`, `MainWindow`).
