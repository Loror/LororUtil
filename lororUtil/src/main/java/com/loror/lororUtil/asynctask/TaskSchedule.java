package com.loror.lororUtil.asynctask;

public class TaskSchedule {

    private Schedule schedule;
    private Catcher catcher;

    public TaskSchedule schedule(Schedule schedule) {
        this.schedule = schedule;
        return this;
    }

    public TaskSchedule ioSchedule() {
        this.schedule = new IoSchedule();
        return this;
    }

    public TaskSchedule mainHandlerSchedule() {
        this.schedule = new MainHandlerSchedule();
        return this;
    }

    public TaskSchedule catcher(Catcher catcher) {
        this.catcher = catcher;
        return this;
    }

    protected Schedule nextSchedule() {
        return schedule;
    }

    protected Catcher catcher() {
        return catcher;
    }
}
