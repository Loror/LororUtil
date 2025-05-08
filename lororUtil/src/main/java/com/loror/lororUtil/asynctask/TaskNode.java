package com.loror.lororUtil.asynctask;

class TaskNode<T1, T2> {

    interface FuncNode<T1, T2> {
        T2 func(T1 it);
    }

    private final FuncNode<T1, T2> funcNode;
    private final Catcher catcher;
    private final Schedule schedule;

    TaskNode(FuncNode<T1, T2> funcNode, Catcher catcher, Schedule schedule) {
        this.schedule = schedule;
        this.catcher = catcher;
        this.funcNode = funcNode;
    }

    T2 call(T1 it) {
        if (catcher != null) {
            try {
                return funcNode.func(it);
            } catch (Exception e) {
                catcher.catchException(e);
                return null;
            }
        } else {
            return funcNode.func(it);
        }
    }

    public Schedule getSchedule() {
        return schedule != null ? schedule : new Schedule() {
            @Override
            public void schedule(Runnable runnable) {
                runnable.run();
            }
        };
    }
}
