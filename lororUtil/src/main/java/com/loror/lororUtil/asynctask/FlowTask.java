package com.loror.lororUtil.asynctask;

import java.util.LinkedList;

public class FlowTask extends TaskSchedule {

    @Override
    public FlowTask ioSchedule() {
        super.ioSchedule();
        return this;
    }

    @Override
    public FlowTask mainHandlerSchedule() {
        super.mainHandlerSchedule();
        return this;
    }

    @Override
    public FlowTask schedule(Schedule schedule) {
        super.schedule(schedule);
        return this;
    }

    @Override
    public FlowTask catcher(Catcher catcher) {
        super.catcher(catcher);
        return this;
    }

    /**
     * 创建链任务
     */
    public <T> Task<T> create(final Func0<T> func) {
        LinkedList<TaskNode> taskNodes = new LinkedList<>();
        taskNodes.add(new TaskNode<>(new TaskNode.FuncNode<Object, T>() {
            @Override
            public T func(Object it) {
                return func.func();
            }
        }, catcher(), nextSchedule()));
        return new Task<T>(taskNodes);
    }

}
