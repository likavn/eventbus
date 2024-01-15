package com.github.likavn.eventbus.schedule;

import java.util.concurrent.ScheduledFuture;

/**
 * @author likavn
 * @date 2024/1/12
 **/
public class ScheduledTaskHolder {
    /**
     * 具体任务
     */
    private Task task;
    /**
     * result of scheduling
     */
    private ScheduledFuture<?> scheduledFuture;


    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public ScheduledFuture<?> getScheduledFuture() {
        return scheduledFuture;
    }

    public void setScheduledFuture(ScheduledFuture<?> scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    public boolean terminate() {
        return scheduledFuture.isCancelled();
    }
}
