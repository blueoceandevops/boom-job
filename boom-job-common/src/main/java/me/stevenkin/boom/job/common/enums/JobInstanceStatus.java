package me.stevenkin.boom.job.common.enums;

import lombok.Data;
import lombok.Getter;

public enum JobInstanceStatus {
    RUNNING(0, "running"),
    SUCCESS(1, "success"),
    FAILED(2, "failed"),
    TIMEOUT(3, "timeout");
    @Getter
    private Integer code;
    @Getter
    private String message;

    JobInstanceStatus(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static JobInstanceStatus fromCode(Integer code) {
        for (JobInstanceStatus jobInstanceStatus : values()) {
            if (jobInstanceStatus.getCode().equals(code)) {
                return jobInstanceStatus;
            }
        }
        throw new IllegalArgumentException();
    }
}
