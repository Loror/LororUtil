package com.loror.lororUtil.asynctask;

import java.util.Iterator;
import java.util.LinkedList;

public class Task<T> extends TaskSchedule {

    private final LinkedList<TaskNode> taskNodes;

    public Task(LinkedList<TaskNode> taskNodes) {
        if (taskNodes == null) {
            this.taskNodes = new LinkedList<>();
        } else {
            this.taskNodes = taskNodes;
        }
    }

    @Override
    public Task<T> ioSchedule() {
        super.ioSchedule();
        return this;
    }

    @Override
    public Task<T> mainHandlerSchedule() {
        super.mainHandlerSchedule();
        return this;
    }

    @Override
    public Task<T> schedule(Schedule schedule) {
        super.schedule(schedule);
        return this;
    }

    @Override
    public Task<T> catcher(Catcher catcher) {
        super.catcher(catcher);
        return this;
    }

    /**
     * 添加链任务
     */
    public <T1> Task<T1> map(final Func<T, T1> func) {
        taskNodes.add(new TaskNode<>(new TaskNode.FuncNode<T, T1>() {
            @Override
            public T1 func(T it) {
                return func.func(it);
            }
        }, catcher(), nextSchedule()));
        return new Task<>(taskNodes);
    }

    /**
     * 调用后开始执行链
     */
    public void call(final Func1<T> func) {
        taskNodes.add(new TaskNode<>(new TaskNode.FuncNode<T, Object>() {
            @Override
            public Object func(T it) {
                func.func(it);
                return null;
            }
        }, catcher(), nextSchedule()));
        Iterator<TaskNode> iterator = taskNodes.iterator();
        Object[] ref = new Object[1];
        execute(iterator, ref);
    }

    private void execute(final Iterator<TaskNode> iterator, final Object[] ref) {
        if (iterator.hasNext()) {
            final TaskNode taskNode = iterator.next();
            taskNode.getSchedule().schedule(new Runnable() {
                @Override
                public void run() {
                    ref[0] = taskNode.call(ref[0]);
                    execute(iterator, ref);
                }
            });
        }
    }

}
