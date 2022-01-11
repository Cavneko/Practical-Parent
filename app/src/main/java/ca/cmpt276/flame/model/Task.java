package ca.cmpt276.flame.model;

/**
 * Task represents a single task. A hash map of tasks is managed by the TaskManager.
 * Each task contains a ChildrenQueue to keep track of who goes next. Each task is
 * given an incrementing ID so that it can be uniquely referenced by other classes.
 */
public class Task {
    public static final long NONE = 0L;
    private final long taskId;
    private final ChildrenQueue childrenQueue = new ChildrenQueue();
    private String name;
    private String desc;

    protected Task(String name, String desc) {
        taskId = TaskManager.getInstance().getNextTaskId();
        setName(name);
        setDesc(desc);
    }

    public long getId() {
        return taskId;
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        if(name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name must be non-empty");
        }

        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    protected void setDesc(String desc) {
        if(desc == null) {
            throw new IllegalArgumentException("Description must be non-null");
        }

        this.desc = desc;
    }

    public Child getNextChild() {
        return childrenQueue.getNext();
    }

    protected void takeTurn() {
        childrenQueue.takeTurn();
    }

    @Override
    public String toString() {
        return name;
    }
}
