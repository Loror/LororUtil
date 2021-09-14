package com.loror.lororUtil.asynctask;

import java.util.LinkedList;

public class TaskSchedule {

    private final LinkedList<Schedule> schedules = new LinkedList<>();
    private Catcher catcher;

    public TaskSchedule schedule(Schedule schedule) {
        schedules.add(schedule);
        return this;
    }

    public TaskSchedule ioSchedule() {
        schedules.add(new IoSchedule());
        return this;
    }

    public TaskSchedule mainHandlerSchedule() {
        schedules.add(new MainHandlerSchedule());
        return this;
    }

    public TaskSchedule catcher(Catcher catcher) {
        this.catcher = catcher;
        return this;
    }

    protected Schedule nextSchedule() {
        if (schedules.size() > 0) {
            return schedules.removeFirst();
        }
        return null;
    }

    protected Catcher catcher() {
        return catcher;
    }
}
