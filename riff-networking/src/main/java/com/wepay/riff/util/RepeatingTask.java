package com.wepay.riff.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public abstract class RepeatingTask {

    private enum TaskState {
        NEW, RUNNING, STOPPED
    }

    private AtomicReference<TaskState> state = new AtomicReference<>(TaskState.NEW);
    private final Thread thread;
    private final CompletableFuture<Boolean> shutdownFuture = new CompletableFuture<>();

    protected RepeatingTask(String taskName) {
        this.thread = new Thread(() -> {
            if (isRunning()) {
                try {
                    init();
                    while (isRunning()) {
                        try {
                            task();
                        } catch (InterruptedException ex) {
                            interrupted(ex);
                        } catch (Throwable ex) {
                            exceptionCaught(ex);
                        }
                    }
                } catch (Throwable ex) {
                    stop();
                    exceptionCaught(ex);
                }
            }
            shutdownFuture.complete(true);
        });
        this.thread.setName(this.thread.getName() + "-" + taskName);
        this.thread.setDaemon(true);
    }

    public void start() {
        if (state.compareAndSet(TaskState.NEW, TaskState.RUNNING)) {
            thread.start();
        } else {
            throw new IllegalStateException("task already started");
        }
    }

    public CompletableFuture<Boolean> stop() {
        if (state.compareAndSet(TaskState.NEW, TaskState.STOPPED) || state.compareAndSet(TaskState.RUNNING, TaskState.STOPPED)) {
            shutdownFuture.complete(true);
        }
        return shutdownFuture;
    }

    public boolean isRunning() {
        return state.get() == TaskState.RUNNING;
    }

    protected void init() throws Exception {
    }

    protected abstract void task() throws Exception;

    protected void interrupted(InterruptedException ex) {
        Thread.interrupted();
    }

    protected abstract void exceptionCaught(Throwable ex);

}
