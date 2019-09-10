package me.stevenkin.boom.job.common.enums;

import lombok.Getter;

public enum JobInstanceShardStatus {
    NEW(0, "NEW"),
    RUNNING(1, "RUNNING"),
    SUCCESS(2, "SUCCESS"),
    FAILED(3, "FAILED");
    @Getter
    private Integer code;
    @Getter
    private String message;

    JobInstanceShardStatus(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static JobInstanceShardStatus fromCode(Integer code) {
        for (JobInstanceShardStatus jobInstanceShardStatus : values()) {
            if (jobInstanceShardStatus.getCode().equals(code)) {
                return jobInstanceShardStatus;
            }
        }
        throw new IllegalArgumentException();
    }


}
