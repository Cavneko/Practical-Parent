package ca.cmpt276.flame.model;

import androidx.annotation.NonNull;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * TaskManager is a singleton that manages multiple Task objects.
 * It is saved to SharedPreferences so that tasks persist when the
 * app is closed and restarted.
 */
public class TaskManager implements Iterable<Task> {
    private static final String SHARED_PREFS_KEY = "SHARED_PREFS_TASK_MANAGER";
    private static TaskManager taskManager;
    private long nextTaskId = 1L;
    private final LinkedHashMap<Long, Task> tasks = new LinkedHashMap<>();

    // Singleton

    public static TaskManager getInstance() {
        if(taskManager == null) {
            taskManager = (TaskManager) PrefsManager.restoreObj(SHARED_PREFS_KEY, TaskManager.class);
        }

        return taskManager;
    }

    // Normal class

    private TaskManager() {
        // singleton: prevent other classes from creating new ones
    }

    public Task getTask(long taskId) {
        if(tasks.containsKey(taskId)) {
            return tasks.get(taskId);
        }

        return null;
    }

    public void addTask(String name, String desc) {
        Task task = new Task(name, desc);
        tasks.put(task.getId(), task);
        persistToSharedPrefs();
    }

    public void modifyTask(Task task, String name, String desc) {
        checkValidTask(task);
        task.setName(name);
        task.setDesc(desc);
        persistToSharedPrefs();
    }

    public void removeTask(Task task) {
        checkValidTask(task);
        tasks.remove(task.getId());
        persistToSharedPrefs();
    }

    public void takeTurn(Task task) {
        checkValidTask(task);
        task.takeTurn();
        persistToSharedPrefs();
    }

    protected long getNextTaskId() {
        return nextTaskId++;
    }

    private void checkValidTask(Task task) {
        if(task == null) {
            throw new IllegalArgumentException("TaskManager expects non-null task");
        } else {
            checkValidTaskId(task.getId());
        }
    }

    private void checkValidTaskId(long taskId) {
        if(!tasks.containsKey(taskId)) {
            throw new IllegalArgumentException("TaskManager expects ID to correspond to valid task");
        }
    }

    private void persistToSharedPrefs() {
        PrefsManager.persistObj(SHARED_PREFS_KEY, this);
    }

    @NonNull
    @Override
    public Iterator<Task> iterator() {
        return tasks.values().iterator();
    }
}
