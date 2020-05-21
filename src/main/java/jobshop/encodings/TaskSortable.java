package jobshop.encodings;

import jobshop.Instance;

import java.util.Comparator;

public class TaskSortable implements Comparator<Task> {
    private final Instance instance;
    public TaskSortable (Instance instance) {
        this.instance=instance;
    }
    @Override
    public int compare(Task task, Task t1) {
        return instance.duration(task) - instance.duration(t1);
    }
}
