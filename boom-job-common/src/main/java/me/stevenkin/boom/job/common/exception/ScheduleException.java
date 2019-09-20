package me.stevenkin.boom.job.common.exception;

public class ScheduleException extends RuntimeException {
    public ScheduleException(Throwable cause) {
        super(cause);
    }

    public ScheduleException() {
    }
}
