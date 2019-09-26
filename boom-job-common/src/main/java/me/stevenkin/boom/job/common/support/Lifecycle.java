package me.stevenkin.boom.job.common.support;

import static me.stevenkin.boom.job.common.support.ComponentStatus.*;

public abstract class Lifecycle {
    private ComponentStatus status;

    public Lifecycle() {
        status = NEW;
    }

    public synchronized void start() throws Exception {
        if (status == RUNNING) {
            return;
        }
        if (!LifeStatusMachine.isLegalStatus(status, RUNNING)) {
            throw new IllegalStateException();
        }
        doStart();
        status = RUNNING;
    }

    public synchronized void pause() throws Exception {
        if (status == PAUSED) {
            return;
        }
        if (!LifeStatusMachine.isLegalStatus(status, PAUSED)) {
            throw new IllegalStateException();
        }
        doPause();
        status = PAUSED;
    }

    public synchronized void resume() throws Exception {
        if (status == RUNNING) {
            return;
        }
        if (!LifeStatusMachine.isLegalStatus(status, RUNNING)) {
            throw new IllegalStateException();
        }
        doResume();
        status = RUNNING;
    }

    public synchronized void shutdown() throws Exception {
        if (status == CLOSED) {
            return ;
        }
        if (!LifeStatusMachine.isLegalStatus(status, CLOSED)) {
            throw new IllegalStateException();
        }
        if (status == NEW) {
            status = CLOSED;
            return ;
        }
        if (status == RUNNING) {
            pause();
        }
        doShutdown();
        status = CLOSED;
    }

    public abstract void doStart() throws Exception;

    public abstract void doPause() throws Exception;

    public abstract void doResume() throws Exception;

    public abstract void doShutdown() throws Exception;
}
